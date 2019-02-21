package io.makerplayground.generator.source;

import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;
import io.makerplayground.util.AzureCognitiveServices;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RaspberryPiCodeGenerator {

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";

    private final Project project;
    private final StringBuilder builder = new StringBuilder();
    private final List<Scene> allSceneUsed;
    private final List<Condition> allConditionUsed;

    private RaspberryPiCodeGenerator(Project project) {
        this.project = project;
        Set<NodeElement> allNodeUsed = Utility.getAllUsedNodes(project);
        this.allSceneUsed = Utility.takeScene(allNodeUsed);
        this.allConditionUsed = Utility.takeCondition(allNodeUsed);
    }

    static SourceCodeResult generateCode(Project project) {
        RaspberryPiCodeGenerator generator = new RaspberryPiCodeGenerator(project);
        // Check if the diagram (only the connected nodes) are all valid.
        if (!Utility.validateDiagram(project)) {
            return new SourceCodeResult(SourceCodeError.DIAGRAM_ERROR, "-");
        }
        // Check if all used devices are assigned.
        if (DeviceMapper.validateDeviceAssignment(project) != DeviceMapperResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }
        if (project.getCloudPlatformUsed().size() > 1) {
            return new SourceCodeResult(SourceCodeError.MORE_THAN_ONE_CLOUD_PLATFORM, "-");
        }
        generator.appendHeader();
        generator.appendTaskVariables();
        generator.appendBeginFunction();
        generator.appendSceneFunctions();
        generator.appendConditionFunctions();
        generator.appendMainCode();
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendHeader() {
        builder.append("import time").append(NEW_LINE);
        builder.append("from MakerPlayground import MP").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream().map(projectDevice -> projectDevice.getActualDevice().getMpLibrary(project.getPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(parseImportStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    private void appendTaskVariables() {
        Set<ProjectDevice> devices = Utility.getUsedDevicesWithTask(project);
        if (!devices.isEmpty()) {
            for (ProjectDevice projectDevice : devices) {
                builder.append(parseDeviceExpressionVariableName(projectDevice)).append(" = [None]");
                long noExpr = Utility.getMaximumNumberOfExpression(project, projectDevice);
                if (noExpr > 1) {
                    builder.append(" * ").append(noExpr);
                }
                builder.append(NEW_LINE);
            }
            builder.append(NEW_LINE);
        }
    }

    private void appendBeginFunction() {
        Begin begin = project.getBegin();

        List<NodeElement> adjacentVertices = Utility.findAdjacentNodes(project, begin);
        List<Scene> adjacentScene = Utility.takeScene(adjacentVertices);
        List<Condition> adjacentCondition = Utility.takeCondition(adjacentVertices);

        // generate code for begin
        builder.append(NEW_LINE);
        builder.append("def ").append(parseSceneFunctionName(begin)).append("():").append(NEW_LINE);
        if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
            if (adjacentScene.size() != 1) {
                throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
            }
            Scene currentScene = adjacentScene.get(0);
            builder.append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(currentScene)).append(NEW_LINE);
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            builder.append(INDENT).append("MP.currentNode = ").append(parseConditionFunctionName(project.getBegin())).append(NEW_LINE);
        } else {
            // do nothing if there isn't any scene or condition
            builder.append(INDENT).append("pass").append(NEW_LINE);
        }
        builder.append(NEW_LINE);
    }

    private void appendSceneFunctions() {
        Queue<NodeElement> nodeToTraverse = new ArrayDeque<>();
        Set<NodeElement> visitedNodes = new HashSet<>();
        List<NodeElement> adjacentNodes;
        List<Scene> adjacentScene;
        List<Condition> adjacentCondition;
        nodeToTraverse.add(project.getBegin());
        while (!nodeToTraverse.isEmpty()) {
            // Remove node from queue
            NodeElement node = nodeToTraverse.remove();
            // There can be the node that already visited after we add to traversing list.
            if(visitedNodes.contains(node)) {
                continue;
            }
            // Add to visited set
            visitedNodes.add(node);
            // Add next unvisited node to queue
            adjacentNodes = Utility.findAdjacentNodes(project, node);
            adjacentScene = Utility.takeScene(adjacentNodes);
            adjacentCondition = Utility.takeCondition(adjacentNodes);
            nodeToTraverse.addAll(adjacentScene.stream().filter(scene -> !visitedNodes.contains(scene)).collect(Collectors.toSet()));
            nodeToTraverse.addAll(adjacentCondition.stream().filter(condition -> !visitedNodes.contains(condition)).collect(Collectors.toSet()));

            // Generate code for node
            if (node instanceof Scene) {
                Scene currentScene = (Scene) node;
                // create function header
                builder.append(NEW_LINE);
                builder.append("def ").append(parseSceneFunctionName(currentScene)).append("():").append(NEW_LINE);
                // do action
                for (UserSetting setting : currentScene.getSetting()) {
                    ProjectDevice device = setting.getDevice();
                    List<String> taskParameter = new ArrayList<>();

                    List<Parameter> parameters = setting.getAction().getParameter();
                    if (setting.isDataBindingUsed()) {  // generate task based code for performing action continuously in background
                        int parameterIndex = 0;
                        for (Parameter p : parameters) {
                            Expression e = setting.getValueMap().get(p);
                            if (setting.isDataBindingUsed(p)) {
                                parameterIndex++;
                                builder.append(INDENT).append("MP.setExpression('").append(parseDeviceName(device)).append("', ")
                                        .append(parameterIndex)
                                        .append("lambda:").append(parseExpressionForParameter(p, e)).append(", ")
                                        .append(parseRefreshInterval(e)).append(")").append(NEW_LINE);
                                taskParameter.add(parseDeviceExpressionVariableName(device) + "[" + parameterIndex + "].value");
                            } else {
                                taskParameter.add(parseExpressionForParameter(p, e));
                            }
                        }
                        for (int i = parameterIndex; i < Utility.getMaximumNumberOfExpression(project, setting.getDevice()); i++) {
                            builder.append(INDENT).append("MP.clearExpression('").append(parseDeviceName(device))
                                    .append("', ").append(i).append(")").append(NEW_LINE);
                        }
                        builder.append(INDENT).append("MP.setTask('").append(parseDeviceName(device)).append("', lambda: ")
                                .append(parseDeviceVariableName(device)).append(".").append(setting.getAction().getFunctionName()).append("(")
                                .append(String.join(", ", taskParameter)).append("))").append(NEW_LINE);
                    } else {    // generate code to perform action once
                        // clear task if this device used to have background task set
                        if (Utility.getUsedDevicesWithTask(project).contains(device)) {
                            builder.append(INDENT).append("MP.unsetTask('").append(parseDeviceName(device)).append("')").append(NEW_LINE);
                        }
                        // generate code to perform the action
                        for (Parameter p : parameters) {
                            taskParameter.add(parseExpressionForParameter(p, setting.getValueMap().get(p)));
                        }
                        builder.append(INDENT).append(parseDeviceVariableName(device)).append(".").append(setting.getAction().getFunctionName())
                                .append("(").append(String.join(", ", taskParameter)).append(")").append(NEW_LINE);
                    }
                }

                // delay
                if (currentScene.getDelay() != 0) {
                    double delayDuration = 0;  // in ms
                    if (currentScene.getDelayUnit() == Scene.DelayUnit.Second) {
                        delayDuration = currentScene.getDelay();
                    } else if (currentScene.getDelayUnit() == Scene.DelayUnit.MilliSecond) {
                        delayDuration = currentScene.getDelay() / 1000.0;
                    }
                    builder.append(INDENT).append("delayEndTime = time.time() + ").append(delayDuration).append(NEW_LINE);
                    builder.append(INDENT).append("while time.time() < delayEndTime:").append(NEW_LINE);
                    builder.append(INDENT).append(INDENT).append("MP.update()").append(NEW_LINE);
                }

                if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                    if (adjacentScene.size() != 1) {
                        throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                    }
                    Scene s = adjacentScene.get(0);
                    builder.append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(s)).append(NEW_LINE);
                } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                    builder.append(INDENT).append("MP.currentNode = ").append(parseConditionFunctionName(currentScene)).append(NEW_LINE);
                } else {
                    builder.append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(project.getBegin())).append(NEW_LINE);
                }

                // end of scene's function
                builder.append(NEW_LINE);
            }
        }
    }

    private void appendConditionFunctions() {
        Queue<NodeElement> nodeToTraverse = new ArrayDeque<>();
        Set<NodeElement> visitedNodes = new HashSet<>();
        List<NodeElement> adjacentNodes;
        List<Scene> adjacentScene;
        List<Condition> adjacentCondition;
        nodeToTraverse.add(project.getBegin());
        while (!nodeToTraverse.isEmpty()) {
            // Remove node from queue
            NodeElement node = nodeToTraverse.remove();
            // There can be the node that already visited after we add to traversing list.
            if(visitedNodes.contains(node)) {
                continue;
            }
            // Add to visited set
            visitedNodes.add(node);
            // Add next unvisited node to queue
            adjacentNodes = Utility.findAdjacentNodes(project, node);
            adjacentScene = Utility.takeScene(adjacentNodes);
            adjacentCondition = Utility.takeCondition(adjacentNodes);
            nodeToTraverse.addAll(adjacentScene.stream().filter(scene -> !visitedNodes.contains(scene)).collect(Collectors.toList()));
            nodeToTraverse.addAll(adjacentCondition.stream().filter(condition -> !visitedNodes.contains(condition)).collect(Collectors.toList()));

            if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                Map<ProjectDevice, Set<Value>> valueUsed = new HashMap<>();
                for (Condition condition : adjacentCondition) {
                    for (UserSetting setting : condition.getSetting()) {
                        Map<ProjectDevice, Set<Value>> tmp = setting.getAllValueUsed(EnumSet.allOf(DataType.class));
                        // merge tmp into valueUsed
                        for (ProjectDevice projectDevice : tmp.keySet()) {
                            if (!valueUsed.containsKey(projectDevice)) {
                                valueUsed.put(projectDevice, new HashSet<>());
                            }
                            valueUsed.get(projectDevice).addAll(tmp.get(projectDevice));
                        }
                    }
                }

                builder.append(NEW_LINE);
                builder.append("def ").append(parseConditionFunctionName(node)).append("():").append(NEW_LINE);

                // generate if for each condition
                for (Condition condition : adjacentCondition) {
                    List<String> booleanExpressions = new ArrayList<>();
                    for (UserSetting setting : condition.getSetting()) {
                        if (setting.getAction() == null) {
                            throw new IllegalStateException("UserSetting {" + setting + "}'s action must be set ");
                        }
                        else if (!setting.getAction().getName().equals("Compare")) {
                            List<String> params = new ArrayList<>();
                            setting.getAction().getParameter().forEach(parameter -> params.add(parseExpressionForParameter(parameter, setting.getValueMap().get(parameter))));
                            booleanExpressions.add(parseDeviceVariableName(setting.getDevice()) + "." +
                                    setting.getAction().getFunctionName() + "(" + String.join(",", params) + ")");
                        } else {
                            for (Value value : setting.getExpression().keySet()) {
                                if (setting.getExpressionEnable().get(value)) {
                                    Expression expression = setting.getExpression().get(value);
                                    booleanExpressions.add("(" + parseExpression(expression) + ")");
                                }
                            }
                        }
                    }
                    if (booleanExpressions.isEmpty()) {
                        throw new IllegalStateException("Found an empty condition block: " + condition);
                    }
                    builder.append(INDENT).append("if ").append(String.join(" and ", booleanExpressions)).append(":").append(NEW_LINE);

                    List<NodeElement> nextNodes = Utility.findAdjacentNodes(project, condition);
                    List<Scene> nextScene = Utility.takeScene(nextNodes);
                    List<Condition> nextCondition = Utility.takeCondition(nextNodes);

                    if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                        if (nextScene.size() != 1) {
                            throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                        }
                        Scene s = nextScene.get(0);
                        builder.append(INDENT).append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(s)).append(NEW_LINE);
                    } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                        throw new IllegalStateException("Nested condition is not allowed");
                    } else {
                        builder.append(INDENT).append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(project.getBegin())).append(NEW_LINE);
                    }
                }
                builder.append(NEW_LINE); // end of while loop
            }
        }
    }

    private void appendMainCode() {
        builder.append("if __name__ == '__main__':").append(NEW_LINE);
        builder.append(INDENT).append("try:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("MP.unsetAllPins()").append(NEW_LINE);

        // TODO: instantiate cloud platform
//            for (CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
//                String cloudPlatformLibName = cloudPlatform.getLibName();
//                String specificCloudPlatformLibName = project.getController().getCloudPlatformLibraryName(cloudPlatform);
//
//                List<String> cloudPlatformParameterValues = cloudPlatform.getParameter().stream()
//                        .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
//                builder.append(cloudPlatformLibName).append("* ").append(parseCloudPlatformVariableName(cloudPlatform))
//                        .append(" = new ").append(specificCloudPlatformLibName)
//                        .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
//            }

        Map<ProjectDevice, String> deviceNameMap = project.getAllDeviceUsed().stream()
                .collect(Collectors.toMap(Function.identity(), this::parseConstructorCall));

        deviceNameMap.forEach((key, value) ->
                builder.append(INDENT).append(INDENT).append(parseDeviceVariableName(key))
                        .append(" = ").append(value).append(NEW_LINE)
        );

        builder.append(INDENT).append(INDENT).append("MP.currentNode = ").append(parseSceneFunctionName(project.getBegin())).append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("while True:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("MP.update()").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("MP.currentNode()").append(NEW_LINE);
        builder.append(INDENT).append("except KeyboardInterrupt:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("MP.cleanup()").append(NEW_LINE);
    }

    private String parseConstructorCall(ProjectDevice projectDevice) {
        StringBuilder text = new StringBuilder(projectDevice.getActualDevice().getMpLibrary(project.getPlatform()));

        List<String> args = new ArrayList<>();
        if (!projectDevice.getActualDevice().getConnectivity().contains(Peripheral.NOT_CONNECTED)) {
            // port
            for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
                if ((p.getConnectionType() != ConnectionType.I2C) && (p.getConnectionType() != ConnectionType.MP_I2C)) {
                    List<DevicePort> port = projectDevice.getDeviceConnection().get(p);
                    if (port == null) {
                        throw new IllegalStateException("Port hasn't been selected!!!");
                    }
                    // prefer alias name over the actual port name if existed as the latter is used for displaying to the user
                    for (DevicePort devicePort : port) {
                        if (p.isI2C1() || p.isI2C() || p == Peripheral.RPI_CAMERA) {
                            continue;
                        }
                        if (!devicePort.getAlias().isEmpty()) {
                            if (p.isDual()) {
                                args.addAll(devicePort.getAlias());
                            } else {
                                // TODO: addAll should be used here, so we can avoid the if statement. (Must be checked)
                                args.add(devicePort.getAlias().get(0));
                            }
                        } else {
                            args.add(devicePort.getName());
                        }
                    }
                }
            }
        }
        // property for the generic device
        for (Property p : projectDevice.getActualDevice().getProperty()) {
            Object value = projectDevice.getPropertyValue(p);
            if (value == null) {
                throw new IllegalStateException("Property hasn't been set");
            }
            switch (p.getDataType()) {
                case INTEGER:
                case DOUBLE:
                    args.add(String.valueOf(((NumberWithUnit) value).getValue()));
                    break;
                case INTEGER_ENUM:
                    args.add(String.valueOf(value));
                    break;
                case STRING:
                case ENUM:
                    args.add("\"" + value.toString() + "\"");
                    break;
                case AZURE_COGNITIVE_KEY:
                    AzureCognitiveServices service = (AzureCognitiveServices) value;
                    args.add("\"" + service.getLocation() + "\"");
                    args.add("\"" + service.getName() + "\"");
                    args.add("\"" + service.getKey1() + "\"");
                    break;
                case DATETIME:
                default:
                    throw new IllegalStateException("Property (" + value + ") hasn't been supported yet");
            }
        }
            // TODO: add Cloud Platform instance to arg list
//            CloudPlatform cloudPlatform = projectDevice.getActualDevice().getCloudPlatform();
//            if (cloudPlatform != null) {
//                args.add(parseCloudPlatformVariableName(cloudPlatform));
//            }
        text.append("(").append(String.join(", ", args)).append(")");
        return text.toString();
    }

    // The required digits is at least 6 for GPS's lat, lon values.
    private static final DecimalFormat NUMBER_WITH_UNIT_DF = new DecimalFormat("0.0#####");
    private static String parseTerm(Term term) {
        if (term instanceof NumberWithUnitTerm) {
            NumberWithUnitTerm term1 = (NumberWithUnitTerm) term;
            return NUMBER_WITH_UNIT_DF.format(term1.getValue().getValue());
        } else if (term instanceof OperatorTerm) {
            OperatorTerm term1 = (OperatorTerm) term;
            switch (term1.getValue()) {
                case PLUS:
                    return "+";
                case MINUS:
                    return "-";
                case MULTIPLY:
                    return "*";
                case DIVIDE:
                    return "/";
                case MOD:
                    return "%";
                case GREATER_THAN:
                    return ">";
                case LESS_THAN:
                    return "<";
                case GREATER_THAN_OR_EQUAL:
                    return ">=";
                case LESS_THAN_OR_EQUAL:
                    return "<=";
                case AND:
                    return " and ";
                case OR:
                    return " or ";
                case NOT:
                    return "not ";
                case OPEN_PARENTHESIS:
                    return "(";
                case CLOSE_PARENTHESIS:
                    return ")";
                case EQUAL:
                    return "==";
                case NOT_EQUAL:
                    return "!=";
                default:
                    throw new IllegalStateException("Operator [" + term1.getValue() + "] not supported");
            }
        } else if (term instanceof RTCTerm) {
            RTCTerm term1 = (RTCTerm) term;
            LocalDateTime rtc = term1.getValue().getLocalDateTime();
            throw new IllegalStateException("Datetime is not supported for Python now.");
        } else if (term instanceof StringTerm) {
            StringTerm term1 = (StringTerm) term;
            return "'" + term1.getValue() + "'";
        } else if (term instanceof ValueTerm) {
            ValueTerm term1 = (ValueTerm) term;
            ProjectValue value = term1.getValue();
            return parseProjectValue(value.getDevice(), value.getValue());
        } else {
            throw new IllegalStateException("Not implemented parseTerm for Term [" + term + "]");
        }
    }

    private String parseExpression(Expression expression) {
        return expression.getTerms().stream().map(RaspberryPiCodeGenerator::parseTerm).collect(Collectors.joining(" "));
    }

    private String parseExpressionForParameter(Parameter parameter, Expression expression) {
        String returnValue;
        String exprStr = parseExpression(expression);
        if (expression instanceof NumberWithUnitExpression) {
            returnValue = String.valueOf(((NumberWithUnitExpression) expression).getNumberWithUnit().getValue());
        } else if (expression instanceof CustomNumberExpression) {
            returnValue =  "MP.constrain(" + exprStr + ", " + parameter.getMinimumValue() + "," + parameter.getMaximumValue() + ")";
        } else if (expression instanceof ValueLinkingExpression) {
            ValueLinkingExpression valueLinkingExpression = (ValueLinkingExpression) expression;
            double fromLow = valueLinkingExpression.getSourceLowValue().getValue();
            double fromHigh = valueLinkingExpression.getSourceHighValue().getValue();
            double toLow = valueLinkingExpression.getDestinationLowValue().getValue();
            double toHigh = valueLinkingExpression.getDestinationHighValue().getValue();
            returnValue = "MP.constrain(MP.map(" + parseProjectValue(valueLinkingExpression.getSourceValue().getDevice()
                    , valueLinkingExpression.getSourceValue().getValue()) + ", " + fromLow + ", " + fromHigh
                    + ", " + toLow + ", " + toHigh + "), " + toLow + ", " + toHigh + ")";
        } else if (expression instanceof ProjectValueExpression) {
            ProjectValueExpression projectValueExpression = (ProjectValueExpression) expression;
            ProjectValue projectValue = projectValueExpression.getProjectValue();
            DataType dataType = projectValue.getValue().getType();
            if (dataType == DataType.STRING) {
                returnValue = parseProjectValue(projectValue.getDevice(), projectValue.getValue());
            } else if (dataType == DataType.DOUBLE || dataType == DataType.INTEGER) {
                // TODO: separate the datatype for double and integer
                NumericConstraint valueConstraint = (NumericConstraint) projectValueExpression.getProjectValue().getValue().getConstraint();
                NumericConstraint resultConstraint = valueConstraint.intersect(parameter.getConstraint(), Function.identity());
                returnValue = "MP.constrain(" + exprStr + ", " + resultConstraint.getMin() + ", " + resultConstraint.getMax() + ")";
            } else {
                throw new IllegalStateException("ProjectValueExpression other than INTEGER, DOUBLE and STRING is not support.");
            }
        } else if (expression instanceof SimpleStringExpression) {
            returnValue = "\"" + ((SimpleStringExpression) expression).getString() + "\"";
        } else if (expression instanceof SimpleRTCExpression) {
            returnValue = exprStr;
        } else if (expression instanceof ImageExpression) {
            ProjectValue projectValue = ((ImageExpression) expression).getProjectValue();
            returnValue = parseProjectValue(projectValue.getDevice(), projectValue.getValue());
        }
        else {
            throw new IllegalStateException();
        }
        return returnValue;
    }

    private double parseRefreshInterval(Expression expression) {
        NumberWithUnit interval = expression.getUserDefinedInterval();
        if (interval.getUnit() == Unit.SECOND) {
            return interval.getValue();
        } else if (interval.getUnit() == Unit.MILLISECOND) {
            return interval.getValue() / 1000.0;
        } else {
            throw new IllegalStateException();
        }
    }

    private static String parseImportStatement(String libName) {
        return "from " + libName + " import " + libName;
    }

    private static String parseSceneFunctionName(NodeElement node) {
        if (node instanceof Begin) {
            return "beginScene";
        } else if (node instanceof Scene) {
            return "scene_" + ((Scene)node).getName().replace(" ", "_");
        }
        throw new IllegalStateException("Not support scene function name for {" + node + "}");
    }

    private static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
        if (nodeBeforeConditions instanceof Begin) {
            return "beginScene_conditions";
        } else if (nodeBeforeConditions instanceof Scene) {
            return parseSceneFunctionName(nodeBeforeConditions) + "_conditions";
        } else if (nodeBeforeConditions instanceof Condition) {
            throw new IllegalStateException("Not support condition function name for condition after condition {" + nodeBeforeConditions + "}");
        }
        throw new IllegalStateException("Not support condition function name for {" + nodeBeforeConditions + "}");
    }

//    private static String parseCloudPlatformVariableName(CloudPlatform cloudPlatform) {
//        return "_" + cloudPlatform.getLibName().replace(" ", "_");
//    }

    private static String parseDeviceName(ProjectDevice projectDevice) {
        return "_" + projectDevice.getName().replace(" ", "_");
    }

    private static String parseProjectValue(ProjectDevice projectDevice, Value value) {
        return parseDeviceVariableName(projectDevice) + ".get" + value.getName().replace(" ", "_") + "()";
    }

//    private String parseDeviceTaskVariableName(ProjectDevice device) {
//        return parseDeviceName(device) + "_Task";
//    }

    private static String parseDeviceVariableName(ProjectDevice projectDevice) {
        return "MP.devices['" + parseDeviceName(projectDevice) + "']";
    }

    private String parseDeviceExpressionVariableName(ProjectDevice device) {
        return "MP.expressions['" + parseDeviceName(device) + "']";
    }
}
