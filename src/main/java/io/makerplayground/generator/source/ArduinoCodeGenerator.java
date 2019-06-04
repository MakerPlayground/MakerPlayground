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
import io.makerplayground.util.AzureIoTHubDevice;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ArduinoCodeGenerator {

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";

    private final Project project;
    private final StringBuilder builder = new StringBuilder();
    private final List<Scene> allSceneUsed;
    private final List<Condition> allConditionUsed;

    private ArduinoCodeGenerator(Project project) {
        this.project = project;
        Set<NodeElement> allNodeUsed = Utility.getAllUsedNodes(project);
        this.allSceneUsed = Utility.takeScene(allNodeUsed);
        this.allConditionUsed = Utility.takeCondition(allNodeUsed);
    }

    static SourceCodeResult generateCode(Project project) {
        ArduinoCodeGenerator generator = new ArduinoCodeGenerator(project);
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
        generator.appendNextRunningTime();
        generator.appendPointerVariables();
        generator.appendProjectValue();
        generator.appendFunctionDeclaration();
        generator.appendTaskVariables();
        generator.appendInstanceVariables();
        generator.appendSetupFunction();
        generator.appendLoopFunction();
        generator.appendUpdateFunction();
        for (Begin begin : generator.project.getBegin()) {
            generator.appendBeginFunction(begin);
        }
        generator.appendSceneFunctions();
        generator.appendConditionFunctions();
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendPointerVariables() {
        project.getBegin().forEach(begin -> builder.append("void (*").append(parsePointerName(begin)).append(")(void);").append(NEW_LINE));
    }

    private void appendHeader() {
        builder.append("#include \"MakerPlayground.h\"").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream().filter(ProjectDevice::isActualDeviceSelected)
                .map(projectDevice -> projectDevice.getActualDevice().getMpLibrary(project.getPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(parseIncludeStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }


    private void appendFunctionDeclaration() {
        for (Begin begin : project.getBegin()) {
            // generate function declaration for task node scene
            builder.append("void ").append(parseSceneFunctionName(begin)).append("();").append(NEW_LINE);

            // generate function declaration for first level condition(s) connected to the task node block
            List<Condition> conditions = Utility.findAdjacentConditions(project, begin);
            if (!conditions.isEmpty()) {
                builder.append("void ").append(parseConditionFunctionName(begin)).append("();").append(NEW_LINE);
            }
        }

        // generate function declaration for each scene and their conditions
        for (Scene scene : allSceneUsed) {
            builder.append("void ").append(parseSceneFunctionName(scene)).append("();").append(NEW_LINE);
            List<Condition> adjacentCondition = Utility.findAdjacentConditions(project, scene);
            if (!adjacentCondition.isEmpty()) {
                builder.append("void ").append(parseConditionFunctionName(scene)).append("();").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendInstanceVariables() {
        // create cloud singleton variables
        for (CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
            String cloudPlatformLibName = cloudPlatform.getLibName();
            String specificCloudPlatformLibName = project.getController().getCloudPlatformLibraryName(cloudPlatform);

            List<String> cloudPlatformParameterValues = cloudPlatform.getParameter().stream()
                    .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
            builder.append(cloudPlatformLibName).append("* ").append(parseCloudPlatformVariableName(cloudPlatform))
                    .append(" = new ").append(specificCloudPlatformLibName)
                    .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
        }

        // instantiate object(s) for each device
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            // skip device that share actual device with other project device
            if (!projectDevice.isActualDeviceSelected()) {
                continue;
            }
            builder.append(projectDevice.getActualDevice().getMpLibrary(project.getPlatform()))
                    .append(" ").append(parseDeviceVariableName(projectDevice));
            List<String> args = new ArrayList<>();
            if (!projectDevice.getActualDevice().getConnectivity().contains(Peripheral.NOT_CONNECTED)) {
                // port
                for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
                    if ((p.getConnectionType() != ConnectionType.I2C) && (p.getConnectionType() != ConnectionType.MP_I2C)
                            && (p.getConnectionType() != ConnectionType.UART)) {
                        List<DevicePort> port = projectDevice.getDeviceConnection().get(p);
                        if (port == null) {
                            throw new IllegalStateException("Port hasn't been selected!!!");
                        }
                        // prefer alias name over the actual port name if existed as the latter is used for displaying to the user
                        for (DevicePort devicePort : port) {
                            if (p.isI2C1() || p.isI2C() || p.isSPI()) {
                                continue;
                            }
                            if (!devicePort.getAlias().isEmpty()) {
                                if (p.isDual()) {
                                    args.addAll(devicePort.getAlias());
                                } else if (p.isPrimaryPortOnly()) {
                                    args.add(devicePort.getAlias().get(0));
                                } else if (p.isSecondaryPortOnly()) {
                                    args.add(devicePort.getAlias().get(1));
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
                    case BOOLEAN_ENUM:
                        args.add(String.valueOf(value));
                        break;
                    case STRING:
                    case ENUM:
                        args.add("\"" + value + "\"");
                        break;
                    case AZURE_COGNITIVE_KEY:
                        AzureCognitiveServices acs = (AzureCognitiveServices) value;
                        args.add("\"" + acs.getLocation().toLowerCase() + "\"");
                        args.add("\"" + acs.getKey1() + "\"");
                        break;
                    case AZURE_IOTHUB_KEY:
                        AzureIoTHubDevice azureIoTHubDevice = (AzureIoTHubDevice) value;
                        args.add("\"" + azureIoTHubDevice.getConnectionString() + "\"");
                        break;
                    default:
                        throw new IllegalStateException("Property (" + value + ") hasn't been supported yet");
                }
            }

            // Cloud Platform instance
            CloudPlatform cloudPlatform = projectDevice.getActualDevice().getCloudPlatform();
            if (cloudPlatform != null) {
                args.add(parseCloudPlatformVariableName(cloudPlatform));
            }

            if (!args.isEmpty()) {
                builder.append("(").append(String.join(", ", args)).append(");").append(NEW_LINE);
            } else {
                builder.append(";").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendNextRunningTime() {
        project.getBegin().forEach(taskNode -> builder.append("unsigned long ").append(parseNextRunningTime(taskNode)).append(" = 0;").append(NEW_LINE));
    }

    private void appendProjectValue() {
        Map<ProjectDevice, Set<Value>> variableMap = project.getAllValueUsedMap(EnumSet.of(DataType.DOUBLE, DataType.INTEGER));
        for (ProjectDevice projectDevice : variableMap.keySet()) {
            for (Value v : variableMap.get(projectDevice)) {
                builder.append("double ").append(parseValueVariableName(projectDevice, v)).append(";").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendSetupFunction() {
        // generate setup function
        builder.append("void setup() {").append(NEW_LINE);
        builder.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);

        if (project.getPlatform().equals(Platform.ARDUINO_ESP32)) {
            builder.append(INDENT).append("analogSetWidth(10);").append(NEW_LINE);
        }

        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
            String cloudPlatformVariableName = parseCloudPlatformVariableName(cloudPlatform);
            builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append("->init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(cloudPlatform.getDisplayName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            String variableName = parseDeviceVariableName(projectDevice);
            builder.append(INDENT).append("status_code = ").append(variableName).append(".init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(projectDevice.getName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }
        project.getBegin().forEach(begin -> builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseSceneFunctionName(begin)).append(";").append(NEW_LINE));
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendLoopFunction() {
        builder.append("void loop() {").append(NEW_LINE);
        builder.append(INDENT).append("update();").append(NEW_LINE);
        project.getBegin().forEach(begin -> {
            builder.append(INDENT).append("if (").append(parseNextRunningTime(begin)).append(" <= millis()) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append(parsePointerName(begin)).append("();").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
        });
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendTaskVariables() {
        Set<ProjectDevice> devices = Utility.getUsedDevicesWithTask(project);
        if (!devices.isEmpty()) {
            for (ProjectDevice projectDevice : devices) {
                builder.append("Task ").append(parseDeviceTaskVariableName(projectDevice)).append(" = NULL;").append(NEW_LINE);
                builder.append("Expr ").append(parseDeviceExpressionVariableName(projectDevice))
                        .append("[").append(Utility.getMaximumNumberOfExpression(project, projectDevice)).append("];").append(NEW_LINE);
            }
            builder.append(NEW_LINE);
        }
    }

    private void appendUpdateFunction() {
        builder.append("void update() {").append(NEW_LINE);
        builder.append(INDENT).append("currentTime = millis();").append(NEW_LINE);
        builder.append(NEW_LINE);

        // allow all cloudplatform maintains their own tasks (e.g. connection)
        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
            builder.append(INDENT).append(parseCloudPlatformVariableName(cloudPlatform)).append("->update(currentTime);").append(NEW_LINE);
        }

        // allow all devices to perform their own tasks
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            builder.append(INDENT).append(parseDeviceVariableName(projectDevice)).append(".update(currentTime);").append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        // retrieve all project values
        Map<ProjectDevice, Set<Value>> valueUsed = project.getAllValueUsedMap(EnumSet.of(DataType.DOUBLE, DataType.INTEGER));
        for (ProjectDevice projectDevice : valueUsed.keySet()) {
            for (Value v : valueUsed.get(projectDevice)) {
                builder.append(INDENT).append(parseValueVariableName(projectDevice, v)).append(" = ")
                        .append(parseDeviceVariableName(projectDevice)).append(".get")
                        .append(v.getName().replace(" ", "_").replace(".", "_")).append("();").append(NEW_LINE);
            }
        }
        if (!valueUsed.isEmpty()) {
            builder.append(NEW_LINE);
        }

        // recompute expression's value
        for (ProjectDevice projectDevice : Utility.getUsedDevicesWithTask(project)) {
            builder.append(INDENT).append("evaluateExpression(").append(parseDeviceTaskVariableName(projectDevice)).append(", ")
                    .append(parseDeviceExpressionVariableName(projectDevice)).append(", ")
                    .append(Utility.getMaximumNumberOfExpression(project, projectDevice)).append(");").append(NEW_LINE);
        }
        if (!Utility.getUsedDevicesWithTask(project).isEmpty()) {
            builder.append(NEW_LINE);
        }

        // log status of each devices
        builder.append(INDENT).append("if (currentTime - latestLogTime > MP_LOG_INTERVAL) {").append(NEW_LINE);
        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
            builder.append(INDENT).append(INDENT).append("MP_LOG_P(").append(parseCloudPlatformVariableName(cloudPlatform))
                    .append(", \"").append(cloudPlatform.getDisplayName()).append("\");").append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            builder.append(INDENT).append(INDENT).append("MP_LOG(").append(parseDeviceVariableName(projectDevice))
                    .append(", \"").append(projectDevice.getName()).append("\");").append(NEW_LINE);
        }
        builder.append(INDENT).append(INDENT).append("latestLogTime = millis();").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);
        if (project.getPlatform() == Platform.ARDUINO_ESP8266) {
            builder.append(INDENT).append("yield();").append(NEW_LINE);
        }
        builder.append("}").append(NEW_LINE);
    }

    private void appendBeginFunction(NodeElement nodeElement) {
        List<NodeElement> adjacentVertices = Utility.findAdjacentNodes(project, nodeElement);
        List<Scene> adjacentScene = Utility.takeScene(adjacentVertices);
        List<Condition> adjacentCondition = Utility.takeCondition(adjacentVertices);

        // generate code for begin
        builder.append(NEW_LINE);
        builder.append("void ").append(parseSceneFunctionName(nodeElement)).append("() {").append(NEW_LINE);
        if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
            if (adjacentScene.size() != 1) {
                throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
            }
            Scene currentScene = adjacentScene.get(0);
            builder.append(INDENT).append(parsePointerName(nodeElement)).append(" = ").append(parseSceneFunctionName(currentScene)).append(";").append(NEW_LINE);
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            builder.append(INDENT).append(parsePointerName(nodeElement)).append(" = ").append(parseConditionFunctionName(nodeElement)).append(";").append(NEW_LINE);
        }
        // do nothing if there isn't any scene or condition
        builder.append("}").append(NEW_LINE);
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
                Set<NodeElement> roots = ((Scene) node).getRoots();
                if (roots.size() != 1) {
                    throw new IllegalStateException("Cannot process the node with zero or more than one root");
                }
                NodeElement root = roots.iterator().next();

                // create function header
                builder.append(NEW_LINE);
                builder.append("void ").append(parseSceneFunctionName(currentScene)).append("() {").append(NEW_LINE);
                builder.append(INDENT).append("update();").append(NEW_LINE);
                // do action
                for (UserSetting setting : currentScene.getSetting()) {
                    ProjectDevice device = setting.getDevice();
                    String deviceName = parseDeviceVariableName(device);
                    List<String> taskParameter = new ArrayList<>();

                    List<Parameter> parameters = setting.getAction().getParameter();
                    if (setting.isDataBindingUsed()) {  // generate task based code for performing action continuously in background
                        int parameterIndex = 0;
                        for (Parameter p : parameters) {
                            Expression e = setting.getValueMap().get(p);
                            if (setting.isDataBindingUsed(p)) {
                                String expressionVarName = parseDeviceExpressionVariableName(device) + "[" + parameterIndex + "]";
                                parameterIndex++;
                                builder.append(INDENT).append("setExpression(").append(expressionVarName).append(", ")
                                        .append("[]()->double{").append("return ").append(parseExpressionForParameter(p, e)).append(";}, ")
                                        .append(parseRefreshInterval(e)).append(");").append(NEW_LINE);
                                taskParameter.add(expressionVarName + ".value");
                            } else {
                                taskParameter.add(parseExpressionForParameter(p, e));
                            }
                        }
                        for (int i = parameterIndex; i < Utility.getMaximumNumberOfExpression(project, setting.getDevice()); i++) {
                            builder.append(INDENT).append("clearExpression(").append(parseDeviceExpressionVariableName(device))
                                    .append("[").append(i).append("]);").append(NEW_LINE);
                        }
                        builder.append(INDENT).append("setTask(").append(parseDeviceTaskVariableName(device)).append(", []() -> void {")
                                .append(deviceName).append(".").append(setting.getAction().getFunctionName()).append("(")
                                .append(String.join(", ", taskParameter)).append(");});").append(NEW_LINE);
                    } else {    // generate code to perform action once
                        // clear task if this device used to have background task set
                        if (Utility.getUsedDevicesWithTask(project).contains(device)) {
                            builder.append(INDENT).append(parseDeviceTaskVariableName(device)).append(" = NULL;").append(NEW_LINE);
                        }
                        // generate code to perform the action
                        for (Parameter p : parameters) {
                            taskParameter.add(parseExpressionForParameter(p, setting.getValueMap().get(p)));
                        }
                        builder.append(INDENT).append(deviceName).append(".").append(setting.getAction().getFunctionName())
                                .append("(").append(String.join(", ", taskParameter)).append(");").append(NEW_LINE);
                    }
                }

                // delay
                if (currentScene.getDelay() != 0) {
                    int delayDuration = 0;  // in ms
                    if (currentScene.getDelayUnit() == Scene.DelayUnit.Second) {
                        delayDuration = (int) (currentScene.getDelay() * 1000.0);
                    } else if (currentScene.getDelayUnit() == Scene.DelayUnit.MilliSecond) {
                        delayDuration = (int) currentScene.getDelay();
                    }
                    builder.append(INDENT).append(parseNextRunningTime(root)).append(" = millis() + ").append(delayDuration).append(";").append(NEW_LINE);
                }

                if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                    if (adjacentScene.size() != 1) {
                        throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                    }
                    Scene s = adjacentScene.get(0);
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(s)).append(";").append(NEW_LINE);
                } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseConditionFunctionName(currentScene)).append(";").append(NEW_LINE);
                } else {
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(root)).append(";").append(NEW_LINE);
                }

                // end of scene's function
                builder.append("}").append(NEW_LINE);
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
                NodeElement root;
                if (node instanceof Scene) {
                    Set<NodeElement> roots = ((Scene) node).getRoots();
                    if (roots.size() != 1) {
                        throw new IllegalStateException("Cannot process the node with zero or more than one root");
                    }
                    root = roots.iterator().next();
                }
                else if (node instanceof Begin) {
                    root = node;
                } else {
                    throw new IllegalStateException("Not support operation");
                }

                builder.append(NEW_LINE);
                builder.append("void ").append(parseConditionFunctionName(node)).append("() {").append(NEW_LINE);

                // call the update function
                builder.append(INDENT).append("update();").append(NEW_LINE);
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
                    builder.append(INDENT).append("if").append("(");
                    builder.append(String.join(" && ", booleanExpressions)).append(") {").append(NEW_LINE);

                    List<NodeElement> nextNodes = Utility.findAdjacentNodes(project, condition);
                    List<Scene> nextScene = Utility.takeScene(nextNodes);
                    List<Condition> nextCondition = Utility.takeCondition(nextNodes);

                    if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                        if (nextScene.size() != 1) {
                            throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                        }
                        Scene s = nextScene.get(0);
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(s)).append(";").append(NEW_LINE);
                    } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                        throw new IllegalStateException("Nested condition is not allowed");
                    } else {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseSceneFunctionName(root)).append(";").append(NEW_LINE);
                    }

                    builder.append(INDENT).append("}").append(NEW_LINE); // end of if
                }
                builder.append("}").append(NEW_LINE); // end of while loop
            }
        }
    }

    private String parseExpression(Expression expression) {
        return expression.getTerms().stream().map(this::parseTerm).collect(Collectors.joining(" "));
    }

    private String parseExpressionForParameter(Parameter parameter, Expression expression) {
        String returnValue;
        String exprStr = parseExpression(expression);
        if (expression instanceof NumberWithUnitExpression) {
            returnValue = String.valueOf(((NumberWithUnitExpression) expression).getNumberWithUnit().getValue());
        } else if (expression instanceof CustomNumberExpression) {
            returnValue =  "constrain(" + exprStr + ", " + parameter.getMinimumValue() + "," + parameter.getMaximumValue() + ")";
        } else if (expression instanceof ValueLinkingExpression) {
            ValueLinkingExpression valueLinkingExpression = (ValueLinkingExpression) expression;
            double fromLow = valueLinkingExpression.getSourceLowValue().getValue();
            double fromHigh = valueLinkingExpression.getSourceHighValue().getValue();
            double toLow = valueLinkingExpression.getDestinationLowValue().getValue();
            double toHigh = valueLinkingExpression.getDestinationHighValue().getValue();
            returnValue = "constrain(map(" + parseValueVariableName(valueLinkingExpression.getSourceValue().getDevice()
                    , valueLinkingExpression.getSourceValue().getValue()) + ", " + fromLow + ", " + fromHigh
                    + ", " + toLow + ", " + toHigh + "), " + toLow + ", " + toHigh + ")";
        } else if (expression instanceof ProjectValueExpression) {
            ProjectValueExpression projectValueExpression = (ProjectValueExpression) expression;
            NumericConstraint valueConstraint = (NumericConstraint) projectValueExpression.getProjectValue().getValue().getConstraint();
            NumericConstraint resultConstraint = valueConstraint.intersect(parameter.getConstraint(), Function.identity());
            returnValue = "constrain(" + exprStr + ", " + resultConstraint.getMin() + ", " + resultConstraint.getMax() + ")";
        } else if (expression instanceof SimpleStringExpression) {
            returnValue = "\"" + ((SimpleStringExpression) expression).getString() + "\"";
        } else if (expression instanceof SimpleRTCExpression) {
            returnValue = exprStr;
        } else if (expression instanceof ImageExpression) {
            ProjectValue projectValue = ((ImageExpression) expression).getProjectValue();
            returnValue = parseDeviceVariableName(projectValue.getDevice()) + ".get"
                    + projectValue.getValue().getName().replace(" ", "_") + "()";
        } else if (expression instanceof RecordExpression) {
            returnValue = exprStr;
        } else {
            throw new IllegalStateException();
        }
        return returnValue;
    }

    private String parseNextRunningTime(NodeElement element) {
        if (element instanceof Begin) {
            return ((Begin) element).getName().replace(" ", "_") + "_nextRunningTime";
        }
        throw new IllegalStateException("No next running time for " + element);
    }

    private int parseRefreshInterval(Expression expression) {
        NumberWithUnit interval = expression.getUserDefinedInterval();
        if (interval.getUnit() == Unit.SECOND) {
            return (int) (interval.getValue() * 1000.0);    // accurate down to 1 ms
        } else if (interval.getUnit() == Unit.MILLISECOND) {
            return (int) interval.getValue();   // fraction of a ms is discard
        } else {
            throw new IllegalStateException();
        }
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
                    return "&&";
                case OR:
                    return "||";
                case NOT:
                    return "!";
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
            return "MP_DATETIME(" + rtc.getSecond() + "," + rtc.getMinute() + "," + rtc.getHour() +  "," + rtc.getDayOfMonth() + "," + rtc.getMonth().getValue() + "," + rtc.getYear() + ")";
        } else if (term instanceof StringTerm) {
            StringTerm term1 = (StringTerm) term;
            return "\"" + term1.getValue() + "\"";
        } else if (term instanceof ValueTerm) {
            ValueTerm term1 = (ValueTerm) term;
            ProjectValue value = term1.getValue();
            return parseValueVariableName(value.getDevice(), value.getValue());
        } else if (term instanceof RecordTerm) {
            RecordTerm term1 = (RecordTerm) term;
            return "Record(" + term1.getValue().getEntryList().stream()
                    .map(entry -> "Entry(\"" + entry.getField() + "\", " + parseExpression(entry.getValue()) + ")")
                    .collect(Collectors.joining(",")) + ")";
        } else {
            throw new IllegalStateException("Not implemented parseTerm for Term [" + term + "]");
        }
    }

    private static String parseIncludeStatement(String libName) {
        return "#include \"" + libName + ".h\"";
    }

    private static String parseSceneFunctionName(NodeElement node) {
        if (node instanceof Scene) {
            return "scene_" + ((Scene)node).getName().replace(" ", "_");
        } else if (node instanceof Begin) {
            return "scene_" + ((Begin) node).getName().replace(" ", "_");
        }
        throw new IllegalStateException("Not support scene function name for {" + node + "}");
    }

    private static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "current_" + ((Begin) nodeElement).getName().replace(" ", "_");
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

    private static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
        if (nodeBeforeConditions instanceof Begin || nodeBeforeConditions instanceof Scene) {
            return parseSceneFunctionName(nodeBeforeConditions) + "_conditions";
        } else if (nodeBeforeConditions instanceof Condition) {
            throw new IllegalStateException("Not support condition function name for condition after condition {" + nodeBeforeConditions + "}");
        }
        throw new IllegalStateException("Not support condition function name for {" + nodeBeforeConditions + "}");
    }

    private static String parseCloudPlatformVariableName(CloudPlatform cloudPlatform) {
        return "_" + cloudPlatform.getLibName().replace(" ", "_");
    }

    private static String parseDeviceVariableName(ProjectDevice projectDevice) {
        if (projectDevice.isActualDeviceSelected()) {
            return "_" + projectDevice.getName().replace(" ", "_").replace(".", "_");
        } else if (projectDevice.isMergeToOtherDevice()) {
            return "_" + projectDevice.getParentDevice().getName().replace(" ", "_").replace(".", "_");
        } else {
            throw new IllegalStateException("Actual device of " + projectDevice.getName() + " hasn't been selected!!!");
        }
    }

    private static String parseValueVariableName(ProjectDevice projectDevice, Value value) {
        return parseDeviceVariableName(projectDevice) + "_" + value.getName().replace(" ", "_").replace(".","_");
    }

    private String parseDeviceTaskVariableName(ProjectDevice device) {
        return parseDeviceVariableName(device) + "_Task";
    }

    private String parseDeviceExpressionVariableName(ProjectDevice device) {
        return parseDeviceVariableName(device) + "_Expr";
    }
}
