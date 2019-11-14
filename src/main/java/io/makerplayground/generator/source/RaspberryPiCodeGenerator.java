/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.generator.source;

import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.*;
import io.makerplayground.project.Condition;
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
    private final ProjectConfiguration configuration;
    private final StringBuilder builder = new StringBuilder();
    private final List<Scene> allSceneUsed;
    private final List<Condition> allConditionUsed;

    private static final Set<PinFunction> PIN_FUNCTION_WITH_CODES = Set.of(
            PinFunction.DIGITAL_IN, PinFunction.DIGITAL_OUT,
            PinFunction.ANALOG_IN, PinFunction.ANALOG_OUT,
            PinFunction.PWM_OUT,
            PinFunction.INTERRUPT_LOW, PinFunction.INTERRUPT_HIGH, PinFunction.INTERRUPT_CHANGE, PinFunction.INTERRUPT_RISING, PinFunction.INTERRUPT_FALLING,
            PinFunction.HW_SERIAL_RX, PinFunction.HW_SERIAL_TX, PinFunction.SW_SERIAL_RX, PinFunction.SW_SERIAL_TX
    );

    private RaspberryPiCodeGenerator(Project project) {
        this.project = project;
        this.configuration = project.getProjectConfiguration();
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
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }
        if (project.getCloudPlatformUsed().size() > 1) {
            return new SourceCodeResult(SourceCodeError.MORE_THAN_ONE_CLOUD_PLATFORM, "-");
        }
        generator.appendHeader();
        generator.appendNextRunningTime();
        generator.appendTaskVariables();
        generator.appendBeginFunctions();
        generator.appendSceneFunctions();
        generator.appendConditionFunctions();
        generator.appendMainCode();
        return new SourceCodeResult(generator.builder.toString());
    }


    private void appendNextRunningTime() {
        project.getBegin().forEach(taskNode -> builder.append(parseNextRunningTime(taskNode)).append(" = 0").append(NEW_LINE));
    }

    private String parseNextRunningTime(NodeElement element) {
        if (element instanceof Begin) {
            return "MP." + ((Begin) element).getName().replace(" ", "_") + "_nextRunningTime";
        }
        throw new IllegalStateException("No next running time for " + element);
    }

    private void appendHeader() {
        builder.append("import time").append(NEW_LINE);
        builder.append("from MakerPlayground import MP").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream().filter(projectDevice -> configuration.getActualDevice(projectDevice).isPresent())
                .map(projectDevice -> configuration.getActualDevice(projectDevice).orElseThrow().getMpLibrary(project.getSelectedPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(parseImportStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    private void appendTaskVariables() {
        Set<ProjectDevice> devices = Utility.getUsedDevicesWithTask(project);
        if (!devices.isEmpty()) {
            for (ProjectDevice projectDevice : devices) {
                builder.append(parseDeviceExpressionVariableName(configuration, projectDevice)).append(" = [None]");
                long noExpr = Utility.getMaximumNumberOfExpression(project, projectDevice);
                if (noExpr > 1) {
                    builder.append(" * ").append(noExpr);
                }
                builder.append(NEW_LINE);
            }
            builder.append(NEW_LINE);
        }
    }

    private void appendBeginFunctions() {
        project.getBegin().forEach(begin -> {
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
                builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseSceneFunctionName(currentScene)).append(NEW_LINE);
            } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseConditionFunctionName(begin)).append(NEW_LINE);
            } else {
                // do nothing if there isn't any scene or condition
                builder.append(INDENT).append("pass").append(NEW_LINE);
            }
            builder.append(NEW_LINE);
        });
    }

    private void appendSceneFunctions() {
        Set<NodeElement> visitedNodes = new HashSet<>();
        List<NodeElement> adjacentNodes;
        List<Scene> adjacentScene;
        List<Condition> adjacentCondition;
        Queue<NodeElement> nodeToTraverse = new ArrayDeque<>(project.getBegin());
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
                Begin root = node.getRoot();

                // create function header
                builder.append(NEW_LINE);
                builder.append("def ").append(parseSceneFunctionName(currentScene)).append("():").append(NEW_LINE);
                builder.append(INDENT).append("MP.update()").append(NEW_LINE);
                // do action
                for (UserSetting setting : currentScene.getSetting()) {
                    ProjectDevice device = setting.getDevice();
                    String deviceName = parseDeviceVariableName(configuration, device);
                    List<String> taskParameter = new ArrayList<>();

                    List<Parameter> parameters = setting.getAction().getParameter();
                    if (setting.isDataBindingUsed()) {  // generate task based code for performing action continuously in background
                        int parameterIndex = 0;
                        for (Parameter p : parameters) {
                            Expression e = setting.getParameterMap().get(p);
                            if (setting.isDataBindingUsed(p)) {
                                parameterIndex++;
                                builder.append(INDENT).append("MP.setExpression('").append(parseDeviceName(configuration, device)).append("', ")
                                        .append(parameterIndex)
                                        .append("lambda:").append(parseExpressionForParameter(p, e)).append(", ")
                                        .append(parseRefreshInterval(e)).append(")").append(NEW_LINE);
                                taskParameter.add(parseDeviceExpressionVariableName(configuration, device) + "[" + parameterIndex + "].value");
                            } else {
                                taskParameter.add(parseExpressionForParameter(p, e));
                            }
                        }
                        for (int i = parameterIndex; i < Utility.getMaximumNumberOfExpression(project, setting.getDevice()); i++) {
                            builder.append(INDENT).append("MP.clearExpression('").append(parseDeviceName(configuration, device))
                                    .append("', ").append(i).append(")").append(NEW_LINE);
                        }
                        builder.append(INDENT).append("MP.setTask('").append(parseDeviceName(configuration, device)).append("', lambda: ")
                                .append(deviceName).append(".").append(setting.getAction().getFunctionName()).append("(")
                                .append(String.join(", ", taskParameter)).append("))").append(NEW_LINE);
                    } else {    // generate code to perform action once
                        // unsetDevice task if this device used to have background task set
                        if (Utility.getUsedDevicesWithTask(project).contains(device)) {
                            builder.append(INDENT).append("MP.unsetTask('").append(parseDeviceName(configuration, device)).append("')").append(NEW_LINE);
                        }
                        // generate code to perform the action
                        for (Parameter p : parameters) {
                            taskParameter.add(parseExpressionForParameter(p, setting.getParameterMap().get(p)));
                        }
                        builder.append(INDENT).append(deviceName).append(".").append(setting.getAction().getFunctionName())
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
                    builder.append(INDENT).append(parseNextRunningTime(root)).append(" = time.time() + ").append(delayDuration).append(NEW_LINE);
                }

                if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                    if (adjacentScene.size() != 1) {
                        throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                    }
                    Scene s = adjacentScene.get(0);
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(s)).append(NEW_LINE);
                } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseConditionFunctionName(currentScene)).append(NEW_LINE);
                } else {
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(root)).append(NEW_LINE);
                }

                // end of scene's function
                builder.append(NEW_LINE);
            }
        }
    }

    private void appendConditionFunctions() {
        Set<NodeElement> visitedNodes = new HashSet<>();
        List<NodeElement> adjacentNodes;
        List<Scene> adjacentScene;
        List<Condition> adjacentCondition;
        Queue<NodeElement> nodeToTraverse = new ArrayDeque<>(project.getBegin());
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
                Begin root = node.getRoot();

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

                // call the update function
                builder.append(INDENT).append("MP.update();").append(NEW_LINE);
                // generate if for each condition
                for (Condition condition : adjacentCondition) {
                    List<String> booleanExpressions = new ArrayList<>();
                    for (UserSetting setting : condition.getSetting()) {
                        if (setting.getAction() == null) {
                            throw new IllegalStateException("UserSetting {" + setting + "}'s action must be set ");
                        } else if (!setting.getAction().getName().equals("Compare")) {
                            List<String> params = new ArrayList<>();
                            setting.getAction().getParameter().forEach(parameter -> params.add(parseExpressionForParameter(parameter, setting.getParameterMap().get(parameter))));
                            booleanExpressions.add(parseDeviceVariableName(configuration, setting.getDevice()) + "." +
                                    setting.getAction().getFunctionName() + "(" + String.join(",", params) + ")");
                        } else {
                            for (Value value : setting.getExpression().keySet()) {
                                if (setting.getExpressionEnable().get(value)) {
                                    Expression expression = setting.getExpression().get(value);
                                    booleanExpressions.add("(" + parseTerms(expression.getTerms()) + ")");
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
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(s)).append(NEW_LINE);
                    } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                        throw new IllegalStateException("Nested condition is not allowed");
                    } else {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(root)).append(NEW_LINE);
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
//                String specificCloudPlatformLibName = project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform);
//
//                List<String> cloudPlatformParameterValues = cloudPlatform.getUnmodifiableCloudParameterMap().stream()
//                        .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
//                builder.append(cloudPlatformLibName).append("* ").append(parseCloudPlatformVariableName(cloudPlatform))
//                        .append(" = new ").append(specificCloudPlatformLibName)
//                        .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
//            }

        Map<ProjectDevice, String> deviceNameMap = project.getAllDeviceUsed().stream()
                .collect(Collectors.toMap(Function.identity(), this::parseConstructorCall));

        deviceNameMap.forEach((key, value) ->
                builder.append(INDENT).append(INDENT).append(parseDeviceVariableName(configuration, key))
                        .append(" = ").append(value).append(NEW_LINE)
        );

        project.getBegin().forEach(begin -> builder.append(INDENT).append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseSceneFunctionName(begin)).append(NEW_LINE));
        builder.append(INDENT).append(INDENT).append("while True:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("MP.update()").append(NEW_LINE);
        project.getBegin().forEach(begin -> {
            builder.append(INDENT).append(INDENT).append(INDENT).append("if ").append(parseNextRunningTime(begin)).append(" <= time.time():").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(parsePointerName(begin)).append("()").append(NEW_LINE);
        });
        builder.append(INDENT).append("except KeyboardInterrupt:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("MP.cleanup()").append(NEW_LINE);
    }

    private static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "MP.current_" + ((Begin) nodeElement).getName().replace(" ", "_");
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

    private String parseConstructorCall(ProjectDevice projectDevice) {
        StringBuilder text = new StringBuilder(configuration.getActualDevice(projectDevice).orElseThrow().getMpLibrary(project.getSelectedPlatform()));

        List<String> args = new ArrayList<>();
        if (configuration.getActualDevice(projectDevice).isEmpty()) {
            return "";
        }
        ActualDevice actualDevice = configuration.getActualDevice(projectDevice).get();
        DeviceConnection connection = project.getProjectConfiguration().getDeviceConnection(projectDevice);
        if (connection != DeviceConnection.NOT_CONNECTED) {
            Map<Connection, Connection> connectionMap = connection.getConsumerProviderConnections();
            for (Connection connectionConsume: actualDevice.getConnectionConsumeByOwnerDevice(projectDevice)) {
                Connection connectionProvide = connectionMap.get(connectionConsume);
                for (int i=0; i<connectionConsume.getPins().size(); i++) {
                    Pin pinConsume = connectionConsume.getPins().get(i);
                    Pin pinProvide = connectionProvide.getPins().get(i);
                    if (pinConsume.getFunction().get(0) == PinFunction.NO_FUNCTION) {
                        continue;
                    }
                    List<PinFunction> possibleFunctionConsume = pinConsume.getFunction().get(0).getPossibleConsume();
                    for (PinFunction function: possibleFunctionConsume) {
                        if (pinProvide.getFunction().contains(function)) {
                            if (PIN_FUNCTION_WITH_CODES.contains(function)) {
                                if (!pinProvide.getCodingName().isEmpty()) {
                                    args.add(pinProvide.getCodingName());
                                } else {
                                    args.add(pinProvide.getRefTo());
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        // property for the generic device
        for (Property p : configuration.getActualDevice(projectDevice).orElseThrow().getProperty()) {
            Object value = configuration.getPropertyValue(projectDevice, p);
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
//            CloudPlatform cloudPlatform = projectDevice.getCompatibleDeviceComboItem().getCloudPlatform();
//            if (cloudPlatform != null) {
//                args.add(parseCloudPlatformVariableName(cloudPlatform));
//            }
        text.append("(").append(String.join(", ", args)).append(")");
        return text.toString();
    }

    // The required digits is at least 6 for GPS's lat, lon values.
    private static final DecimalFormat NUMBER_WITH_UNIT_DF = new DecimalFormat("0.0#####");
    private String parseTerm(Term term) {
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
            return parseProjectValue(configuration, value.getDevice(), value.getValue());
        } else {
            throw new IllegalStateException("Not implemented parseTerm for Term [" + term + "]");
        }
    }

    private String parseTerms(List<Term> expression) {
        return expression.stream().map(this::parseTerm).collect(Collectors.joining(" "));
    }

    private String parseExpressionForParameter(Parameter parameter, Expression expression) {
        String returnValue;
        String exprStr = parseTerms(expression.getTerms());
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
            returnValue = "MP.constrain(MP.map(" + parseProjectValue(configuration, valueLinkingExpression.getSourceValue().getDevice()
                    , valueLinkingExpression.getSourceValue().getValue()) + ", " + fromLow + ", " + fromHigh
                    + ", " + toLow + ", " + toHigh + "), " + toLow + ", " + toHigh + ")";
        } else if (expression instanceof ProjectValueExpression) {
            ProjectValueExpression projectValueExpression = (ProjectValueExpression) expression;
            ProjectValue projectValue = projectValueExpression.getProjectValue();
            DataType dataType = projectValue.getValue().getType();
            if (dataType == DataType.STRING) {
                returnValue = parseProjectValue(configuration, projectValue.getDevice(), projectValue.getValue());
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
            returnValue = parseProjectValue(configuration, projectValue.getDevice(), projectValue.getValue());
        } else if (expression instanceof ComplexStringExpression) {
            List<String> subExpression = new ArrayList<>();
            for (Expression e : ((ComplexStringExpression) expression).getSubExpressions()) {
                if (e instanceof SimpleStringExpression) {
                    subExpression.add("'" + ((SimpleStringExpression) e).getString() + "'");
                } else if (e instanceof CustomNumberExpression) {
                    subExpression.add("str(" + parseTerms(e.getTerms()) + ")");
                } else {
                    throw new IllegalStateException(e.getClass().getName() + " is not supported in ComplexStringExpression");
                }
            }
            returnValue = String.join("+", subExpression);
        } else {
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
            return "scene_" + ((Begin)node).getName().replace(" ", "_");
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

    private static String parseDeviceName(ProjectConfiguration configuration, ProjectDevice projectDevice) {
        if (configuration.getIdenticalDevice(projectDevice).isPresent()) {
            return "_" + configuration.getIdenticalDevice(projectDevice).orElseThrow().getName().replace(" ", "_").replace(".", "_");
        } else if (configuration.getActualDevice(projectDevice).isPresent()) {
            return "_" + projectDevice.getName().replace(" ", "_").replace(".", "_");
        } else {
            throw new IllegalStateException("Actual device of " + projectDevice.getName() + " hasn't been selected!!!");
        }
    }

    private static String parseProjectValue(ProjectConfiguration configuration,  ProjectDevice projectDevice, Value value) {
        return parseDeviceVariableName(configuration, projectDevice) + ".get" + value.getName().replace(" ", "_") + "()";
    }

//    private String parseDeviceTaskVariableName(ProjectDevice device) {
//        return parseDeviceName(device) + "_Task";
//    }

    private static String parseDeviceVariableName(ProjectConfiguration configuration, ProjectDevice projectDevice) {
        return "MP.devices['" + parseDeviceName(configuration, projectDevice) + "']";
    }

    private String parseDeviceExpressionVariableName(ProjectConfiguration configuration, ProjectDevice device) {
        return "MP.expressions['" + parseDeviceName(configuration, device) + "']";
    }
}
