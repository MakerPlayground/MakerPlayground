/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.ConnectionType;
import io.makerplayground.device.actual.Peripheral;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceCodeGenerator {

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private final Project project;
    private final boolean cppMode;
    private final StringBuilder builder = new StringBuilder();

    /* these variables are for keeping the result of generateCodeForSceneFunctions() */
    private final Set<Scene> visitedScene = new HashSet<>();
    private final StringBuilder sceneFunctions = new StringBuilder();
    private boolean generateMapFunction;    // use for ValueLinkingExpression as Arduino built-in map function uses integer arithmetic

    private SourceCodeGenerator(Project project, boolean cppMode) {
        this.project = project;
        this.cppMode = cppMode;
    }

    private static String generateInclude(String library) {
        return "#include \"" + library + ".h\"";
    }

    private void appendHeader() {
        builder.append("#include \"MakerPlayground.h\"").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream().map(projectDevice -> projectDevice.getActualDevice().getMpLibrary(project.getPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new))    //remove duplicates
                .forEach(s -> builder.append(generateInclude(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    /* Beware!! this function need the result from generateCodeForSceneFunctions() before run */
    private void appendFunctionDeclaration() {
        // generate function declaration for each scene
        if (cppMode) {
            builder.append("void beginScene();").append(NEW_LINE);
            for (Scene scene : visitedScene) {
                builder.append("void ").append("scene_").append(scene.getName().replace(" ", "_")).append("();").append(NEW_LINE);
            }
            builder.append(NEW_LINE);
        }
    }

    private void appendInstanceVariables() {
        // create cloud singleton variables
        for (CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
            String cloudPlatformLibName = cloudPlatform.getLibName();
            String specificCloudPlatformLibName = project.getController().getCloudPlatformLibraryName(cloudPlatform);

            List<String> cloudPlatformParameterValues = cloudPlatform.getParameter().stream()
                    .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
            builder.append(cloudPlatformLibName).append("* ").append(getCloudPlatformVariableName(cloudPlatform))
                    .append(" = new ").append(specificCloudPlatformLibName)
                    .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
        }

        // instantiate object(s) for each device
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            builder.append(projectDevice.getActualDevice().getMpLibrary(project.getPlatform()))
                    .append(" ").append(getDeviceVariableName(projectDevice));
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
                            if (p.isI2C1() || p.isI2C()) {
                                continue;
                            }
                            if (!devicePort.getAlias().isEmpty()) {
                                if (p.isDual()) {
                                    args.addAll(devicePort.getAlias());
                                } else {
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
                String value = projectDevice.getPropertyValue(p);
                if (value == null) {
                    throw new IllegalStateException("Property hasn't been set");
                }
                switch (p.getDataType()) {
                    case INTEGER:
                    case INTEGER_ENUM:
                    case DOUBLE:
                        args.add(value);
                        break;
                    default:
                        args.add("\"" + value + "\"");
                }
            }

            // Cloud Platform instance
            CloudPlatform cloudPlatform = projectDevice.getActualDevice().getCloudPlatform();
            if (cloudPlatform != null) {
                args.add(getCloudPlatformVariableName(cloudPlatform));
            }

            if (args.size() > 0) {
                builder.append("(").append(String.join(", ", args)).append(");").append(NEW_LINE);
            } else {
                builder.append(";").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendProjectValue() {
        Map<ProjectDevice, Set<Value>> variableMap = project.getAllValueUsedMap();
        for (ProjectDevice projectDevice : variableMap.keySet()) {
            for (Value v : variableMap.get(projectDevice)) {
                builder.append("double ").append(getValueVariableName(projectDevice, v)).append(";").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendSetupFunction() {
        // generate setup function
        builder.append("void setup() {").append(NEW_LINE);
        builder.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);

        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
            String cloudPlatformVariableName = getCloudPlatformVariableName(cloudPlatform);
            builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append("->init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR_P(").append(cloudPlatformVariableName).append(", \"").append(cloudPlatform.getDisplayName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            String variableName = getDeviceVariableName(projectDevice);
            builder.append(INDENT).append("status_code = ").append(variableName).append(".init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(").append(variableName).append(", \"").append(projectDevice.getName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }
        builder.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendTaskVariables() {
        Set<ProjectDevice> devices = getUsedDevicesWithTask();
        if (!devices.isEmpty()) {
            for (ProjectDevice projectDevice : devices) {
                builder.append("Task ").append(getDeviceTaskVariableName(projectDevice)).append(" = NULL;").append(NEW_LINE);
                builder.append("Expr ").append(getDeviceExpressionVariableName(projectDevice))
                        .append("[").append(getMaximumNumberOfExpression(projectDevice)).append("];").append(NEW_LINE);
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
            builder.append(INDENT).append(getCloudPlatformVariableName(cloudPlatform)).append("->update(currentTime);").append(NEW_LINE);
        }

        // allow all devices to perform their own tasks
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            builder.append(INDENT).append(getDeviceVariableName(projectDevice)).append(".update(currentTime);").append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        // retrieve all project values
        Map<ProjectDevice, Set<Value>> valueUsed = project.getAllValueUsedMap();
        for (ProjectDevice projectDevice : valueUsed.keySet()) {
            for(Value v : valueUsed.get(projectDevice)) {
                builder.append(INDENT).append(getValueVariableName(projectDevice, v)).append(" = ")
                        .append(getDeviceVariableName(projectDevice)).append(".get")
                        .append(v.getName().replace(" ", "_")).append("();").append(NEW_LINE);
            }
        }
        if (!valueUsed.isEmpty()) {
            builder.append(NEW_LINE);
        }

        // recompute expression's value
        for (ProjectDevice projectDevice : getUsedDevicesWithTask()) {
            builder.append(INDENT).append("evaluateExpression(").append(getDeviceTaskVariableName(projectDevice)).append(", ")
                    .append(getDeviceExpressionVariableName(projectDevice)).append(", ")
                    .append(getMaximumNumberOfExpression(projectDevice)).append(");").append(NEW_LINE);
        }
        if (!getUsedDevicesWithTask().isEmpty()) {
            builder.append(NEW_LINE);
        }

        // log status of each devices
        builder.append(INDENT).append("if (currentTime - latestLogTime > MP_LOG_INTERVAL) {").append(NEW_LINE);
        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
            builder.append(INDENT).append(INDENT).append("MP_LOG_P(").append(getCloudPlatformVariableName(cloudPlatform))
                    .append(", \"").append(cloudPlatform.getDisplayName()).append("\");").append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            builder.append(INDENT).append(INDENT).append("MP_LOG(").append(getDeviceVariableName(projectDevice))
                    .append(", \"").append(projectDevice.getName()).append("\");").append(NEW_LINE);
        }
        builder.append(INDENT).append(INDENT).append("latestLogTime = millis();").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);
        builder.append("#ifdef ESP8266").append(NEW_LINE);
        builder.append(INDENT).append("yield();").append(NEW_LINE);
        builder.append("#endif").append(NEW_LINE);
        builder.append("}").append(NEW_LINE);
    }

    private Set<ProjectDevice> getUsedDevicesWithTask() {
        return project.getScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(UserSetting::isDataBindingUsed)
                .map(UserSetting::getDevice)
                .collect(Collectors.toSet());
    }

    private void appendSceneFunctions() {
        builder.append(sceneFunctions).append(NEW_LINE);
    }

    public static SourceCodeResult generateCode(Project project, boolean cppMode) {
        SourceCodeGenerator generator = new SourceCodeGenerator(project, cppMode);
        if (!generator.checkDiagram(project)) {
            return new SourceCodeResult(SourceCodeError.DIAGRAM_ERROR, "-");
        }
        if (!generator.checkDeviceAssignment(project)) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!generator.checkDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }
        if (project.getCloudPlatformUsed().size() > 1) {
            return new SourceCodeResult(SourceCodeError.MORE_THAN_ONE_CLOUD_PLATFORM, "-");
        }

        generator.generateCodeForSceneFunctions();

        generator.appendHeader();
        generator.appendProjectValue();
        generator.appendFunctionDeclaration();
        generator.appendTaskVariables();
        generator.appendInstanceVariables();
        generator.appendSetupFunction();
        generator.appendUpdateFunction();
        generator.appendSceneFunctions();

        return new SourceCodeResult(generator.builder.toString());
    }

    private void generateCodeForSceneFunctions() {
        Queue<Scene> queue = new ArrayDeque<>();

        List<NodeElement> adjacentVertices = findAdjacentVertices(project, project.getBegin());
        List<Scene> adjacentScene = getScene(adjacentVertices);
        List<Condition> adjacentCondition = getCondition(adjacentVertices);

        // generate code for begin
        sceneFunctions.append(NEW_LINE);
        sceneFunctions.append("void beginScene() {").append(NEW_LINE);
        if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
            if (adjacentScene.size() == 1) {
                Scene s = adjacentScene.get(0);
                sceneFunctions.append(INDENT).append("currentScene = ").append("scene_").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                visitedScene.add(s);
                queue.add(s);
            } else {
                throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
            }
        } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
            processCondition(sceneFunctions, queue, visitedScene, project, adjacentCondition);
        }
        // do nothing if there isn't any scene or condition
        sceneFunctions.append("}").append(NEW_LINE);

        // generate function for each scene
        while (!queue.isEmpty()) {
            Scene currentScene = queue.remove();

            // create function header
            sceneFunctions.append(NEW_LINE);
            sceneFunctions.append("void ").append("scene_").append(currentScene.getName().replace(" ", "_")).append("() {").append(NEW_LINE);
            sceneFunctions.append(INDENT).append("update();").append(NEW_LINE);

            // do action
            for (UserSetting setting : currentScene.getSetting()) {
                ProjectDevice device = setting.getDevice();
                String deviceName = getDeviceVariableName(device);
                List<String> taskParameter = new ArrayList<>();

                List<Parameter> parameters = setting.getAction().getParameter();
                if (setting.isDataBindingUsed()) {  // generate task based code for performing action continuously in background
                    int parameterIndex = 0;
                    for (Parameter p : parameters) {
                        Expression e = setting.getValueMap().get(p);
                        if (setting.isDataBindingUsed(p)) {
                            String expressionVarName = getDeviceExpressionVariableName(device) + "[" + parameterIndex + "]";
                            parameterIndex++;
                            sceneFunctions.append(INDENT).append("setExpression(").append(expressionVarName).append(", ")
                                    .append("[]()->double{").append("return ").append(parseExpression(p, e)).append(";}, ")
                                    .append(parseRefreshInterval(e)).append(");").append(NEW_LINE);
                            taskParameter.add(expressionVarName + ".value");
                        } else {
                            taskParameter.add(parseExpression(p, e));
                        }
                    }
                    for (int i = parameterIndex; i < getMaximumNumberOfExpression(setting.getDevice()); i++) {
                        sceneFunctions.append(INDENT).append("clearExpression(").append(getDeviceExpressionVariableName(device))
                                .append("[").append(i).append("]);").append(NEW_LINE);
                    }
                    sceneFunctions.append(INDENT).append("setTask(").append(getDeviceTaskVariableName(device)).append(", []() -> void {")
                            .append(deviceName).append(".").append(setting.getAction().getFunctionName()).append("(")
                            .append(String.join(", ", taskParameter)).append(");});").append(NEW_LINE);
                } else {    // generate code to perform action once
                    // clear task if this device used to have background task set
                    if (getUsedDevicesWithTask().contains(device)) {
                        sceneFunctions.append(INDENT).append(getDeviceTaskVariableName(device)).append(" = NULL;").append(NEW_LINE);
                    }
                    // generate code to perform the action
                    for (Parameter p : parameters) {
                        taskParameter.add(parseExpression(p, setting.getValueMap().get(p)));
                    }
                    sceneFunctions.append(INDENT).append(deviceName).append(".").append(setting.getAction().getFunctionName())
                            .append("(").append(String.join(", ", taskParameter)).append(");").append(NEW_LINE);
                }
            }

            // delay
            if (currentScene.getDelay() != 0) {
                int delayDuration = 0;  // in ms
                if (currentScene.getDelayUnit() == Scene.DelayUnit.Second) {
                    delayDuration = (int) (currentScene.getDelay() * 1000);
                } else if (currentScene.getDelayUnit() == Scene.DelayUnit.MilliSecond) {
                    delayDuration = (int) currentScene.getDelay();
                }
                sceneFunctions.append(INDENT).append("delayEndTime = millis() + ").append(delayDuration).append(";").append(NEW_LINE);
                sceneFunctions.append(INDENT).append("while (millis() < delayEndTime) {").append(NEW_LINE);
                sceneFunctions.append(INDENT).append(INDENT).append("update();").append(NEW_LINE);
                sceneFunctions.append(INDENT).append("}").append(NEW_LINE);
            }

            // update list of adjacent vertices (scenes/conditions)
            adjacentVertices = findAdjacentVertices(project, currentScene);
            adjacentScene = getScene(adjacentVertices);
//            adjacentScene = getUnvisitedScene(adjacentVertices, visitedScene);
            adjacentCondition = getCondition(adjacentVertices);

            if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (adjacentScene.size() == 1) {
                    Scene s = adjacentScene.get(0);
                    sceneFunctions.append(INDENT).append("currentScene = ").append("scene_").append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                    if (!visitedScene.contains(s)) {
                        visitedScene.add(s);
                        queue.add(s);
                    }
                } else {
                    throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                }
            } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                processCondition(sceneFunctions, queue, visitedScene, project, adjacentCondition);
            } else {
                sceneFunctions.append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
            }

            // end of scene's function
            sceneFunctions.append("}").append(NEW_LINE);
        }
    }

    private void processCondition(StringBuilder sb, Queue<Scene> queue, Collection<Scene> visitedScene, Project project, List<Condition> adjacentCondition) {
        // gather every value used by every condition connect to the current scene
        Map<ProjectDevice, Set<Value>> valueUsed = new HashMap<>();
        for (Condition condition : adjacentCondition) {
            for (UserSetting setting : condition.getSetting()) {
                Map<ProjectDevice, Set<Value>> tmp = setting.getAllValueUsed();
                // merge tmp into valueUsed
                for (ProjectDevice projectDevice : tmp.keySet()) {
                    if (!valueUsed.containsKey(projectDevice)) {
                        valueUsed.put(projectDevice, new HashSet<>());
                    }
                    valueUsed.get(projectDevice).addAll(tmp.get(projectDevice));
                }
            }
        }


        // loop to check sensor
        sb.append(INDENT).append("while (1) {").append(NEW_LINE);
        // call the update function
        sb.append(INDENT).append(INDENT).append("update();").append(NEW_LINE);
        // generate if for each condition
        for (Condition condition : adjacentCondition) {
            List<String> conditionList = new ArrayList<>();
            for (UserSetting setting : condition.getSetting()) {
                if ((setting.getAction() != null) && !setting.getAction().getName().equals("Compare")) {
                    List<String> params = new ArrayList<>();
                    for (Parameter parameter : setting.getAction().getParameter()) {
                        params.add(parseExpression(parameter, setting.getValueMap().get(parameter)));
                    }
                    conditionList.add("_" + setting.getDevice().getName().replace(" ", "_") + "." +
                            setting.getAction().getFunctionName() + "(" + String.join(",", params) + ")");
                } else {
                    for (Value value : setting.getExpression().keySet()) {
                        if (setting.getExpressionEnable().get(value)) {
                            Expression expression = setting.getExpression().get(value);
                            conditionList.add("(" + expression.translateToCCode() + ")");
                        }
                    }
                }
            }
            if (!conditionList.isEmpty()) {
                sb.append(INDENT).append(INDENT).append("if").append("(");
                sb.append(String.join(" && ", conditionList)).append(") {").append(NEW_LINE);
            } else {
                throw new IllegalStateException("Found an empty condition block");
            }

            List<NodeElement> nextVertices = findAdjacentVertices(project, condition);
            List<Scene> nextScene = getScene(nextVertices);
            List<Condition> nextCondition = getCondition(nextVertices);
            if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (nextScene.size() == 1) {
                    Scene s = nextScene.get(0);
                    sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = ").append("scene_")
                            .append(s.getName().replace(" ", "_")).append(";").append(NEW_LINE);
                    sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
                    if (!visitedScene.contains(s)) {
                        visitedScene.add(s);
                        queue.add(s);
                    }
                } else {
                    throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                }
            } else if (!nextCondition.isEmpty()) { // nest condition is not allowed
                throw new IllegalStateException("Nested condition is not allowed");
            } else {
                sb.append(INDENT).append(INDENT).append(INDENT).append("currentScene = beginScene;").append(NEW_LINE);
                sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
            }

            sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE); // end of if
        }
        sb.append(INDENT).append("}").append(NEW_LINE); // end of while loop
    }

    private String parseExpression(Parameter parameter, Expression expression) {
        String returnValue;
        if (expression instanceof NumberWithUnitExpression) {
            returnValue = String.valueOf(((NumberWithUnitExpression) expression).getNumberWithUnit().getValue());
        } else if (expression instanceof CustomNumberExpression) {
            returnValue =  "constrain(" + expression.translateToCCode() + ", " + parameter.getMinimumValue() + "," + parameter.getMaximumValue() + ")";
        } else if (expression instanceof ValueLinkingExpression) {
            ValueLinkingExpression valueLinkingExpression = (ValueLinkingExpression) expression;
            double fromLow = valueLinkingExpression.getSourceLowValue().getValue();
            double fromHigh = valueLinkingExpression.getSourceHighValue().getValue();
            double toLow = valueLinkingExpression.getDestinationLowValue().getValue();
            double toHigh = valueLinkingExpression.getDestinationHighValue().getValue();
            returnValue = "constrain(map(" + getValueVariableName(valueLinkingExpression.getSourceValue().getDevice()
                    , valueLinkingExpression.getSourceValue().getValue()) + ", " + fromLow + ", " + fromHigh
                    + ", " + toLow + ", " + toHigh + "), " + toLow + ", " + toHigh + ")";
            generateMapFunction = true;
        } else if (expression instanceof ProjectValueExpression) {
            ProjectValueExpression projectValueExpression = (ProjectValueExpression) expression;
            NumericConstraint valueConstraint = (NumericConstraint) projectValueExpression.getProjectValue().getValue().getConstraint();
            NumericConstraint resultConstraint = valueConstraint.intersect(parameter.getConstraint(), Function.identity());
            returnValue = "constrain(" + expression.translateToCCode() + ", " + resultConstraint.getMin() + ", " + resultConstraint.getMax() + ")";
        } else if (expression instanceof SimpleStringExpression) {
            returnValue = "\"" + ((SimpleStringExpression) expression).getString() + "\"";
        } else if (expression instanceof SimpleRTCExpression) {
            returnValue = expression.translateToCCode();
        } else {
            throw new IllegalStateException();
        }
        return returnValue;
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

    private boolean checkDiagram(Project project) {
        return project.getScene().stream().noneMatch(scene -> scene.getError() != DiagramError.NONE)
                && project.getCondition().stream().noneMatch(condition -> condition.getError() != DiagramError.NONE)
                && project.getDiagramStatus().isEmpty();
    }

    private boolean checkDeviceProperty(Project project) {
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // check only device that has a property
            if (!device.getActualDevice().getProperty().isEmpty()) {
                for (Property p : device.getActualDevice().getProperty()) {
                    String value = device.getPropertyValue(p);
                    // TODO: allow property to be optional
                    if (value == null || value.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkDeviceAssignment(Project project) {
        for (ProjectDevice device : project.getAllDeviceUsed()) {
            // indicate error immediately if an actual device hasn't been selected
            if (device.getActualDevice() == null) {
                return false;
            }
            // for each connectivity required, check if it has been connected and indicate error if it hasn't
            for (Peripheral peripheral : device.getActualDevice().getConnectivity()) {
                if (!device.getDeviceConnection().containsKey(peripheral)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<NodeElement> findAdjacentVertices(Project project, NodeElement source) {
        return project.getLine().stream().filter(line -> line.getSource() == source)
                .map(Line::getDestination).collect(Collectors.toList());
    }

    private static List<Scene> getScene(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    private static List<Scene> getUnvisitedScene(List<NodeElement> nodeElements, Collection<Scene> visitedScene) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Scene)
                .filter(nodeElement -> !visitedScene.contains(nodeElement))
                .map(nodeElement -> (Scene) nodeElement).collect(Collectors.toList());
    }

    private static List<Condition> getCondition(List<NodeElement> nodeElements) {
        return nodeElements.stream().filter(nodeElement -> nodeElement instanceof Condition)
                .map(nodeElement -> (Condition) nodeElement).collect(Collectors.toList());
    }

    private static String getCloudPlatformVariableName(CloudPlatform cloudPlatform) {
        return "_" + cloudPlatform.getLibName().replaceAll(" ", "_");
    }

    private static String getDeviceVariableName(ProjectDevice projectDevice) {
        return "_" + projectDevice.getName().replace(" ", "_");
    }

    private static String getValueVariableName(ProjectDevice projectDevice, Value value) {
        return getDeviceVariableName(projectDevice) + "_" + value.getName().replace(" ", "_");
    }

    private String getDeviceTaskVariableName(ProjectDevice device) {
        return getDeviceVariableName(device) + "_Task";
    }

    private String getDeviceExpressionVariableName(ProjectDevice device) {
        return getDeviceVariableName(device) + "_Expr";
    }

    // maximum number of binded parameter in each action of each devices
    private long getMaximumNumberOfExpression(ProjectDevice device) {
        return project.getScene().stream()
                .flatMap(scene -> scene.getSetting().stream())
                .filter(userSetting -> userSetting.getDevice() == device)
                .filter(UserSetting::isDataBindingUsed)
                .mapToLong(UserSetting::getNumberOfDatabindParams)
                .max().orElse(0);
    }


}
