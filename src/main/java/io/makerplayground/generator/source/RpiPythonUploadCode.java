/*
 * Copyright (c) 2020. The Maker Playground Authors.
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
import io.makerplayground.project.VirtualProjectDevice.Memory;
import io.makerplayground.project.VirtualProjectDevice.TimeElapsed;
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

import static io.makerplayground.generator.source.ArduinoCodeUtility.INDENT;
import static io.makerplayground.generator.source.ArduinoCodeUtility.NEW_LINE;
import static io.makerplayground.generator.source.RpiPythonCodeUtility.*;

public class RpiPythonUploadCode {

    private final Project project;
    private final ProjectConfiguration configuration;
    private final StringBuilder builder = new StringBuilder();
    private final List<Scene> allSceneUsed;
    private final List<Condition> allConditionUsed;
    private final List<List<ProjectDevice>> projectDeviceGroup;
    private final List<Delay> allDelayUsed;

    private RpiPythonUploadCode(Project project) {
        this.project = project;
        this.configuration = project.getProjectConfiguration();
        Set<NodeElement> allNodeUsed = Utility.getAllUsedNodes(project);
        this.allSceneUsed = Utility.takeScene(allNodeUsed);
        this.allConditionUsed = Utility.takeCondition(allNodeUsed);
        this.allDelayUsed = Utility.takeDelay(allNodeUsed);
        this.projectDeviceGroup = project.getAllDeviceUsedGroupBySameActualDevice();
    }

    public static SourceCodeResult generateCode(Project project) {
        RpiPythonUploadCode generator = new RpiPythonUploadCode(project);
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
        generator.appendBeginRecentSceneFinishTime();
        generator.appendBeginFunctions();
        generator.appendSceneFunctions();
        generator.appendConditionFunctions();
        generator.appendGenMessageFunction();
        generator.appendMainCode();
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendBeginRecentSceneFinishTime() {
        project.getBegin().forEach(taskNode -> builder.append(parseBeginRecentSceneFinishTime(taskNode)).append(" = 0").append(NEW_LINE));
    }

    private String parseBeginRecentSceneFinishTime(Begin begin) {
        return "MP." + begin.getName().replace(" ", "_") + "_recentSceneFinishTime";
    }

    private void appendHeader() {
        builder.append("import time").append(NEW_LINE);
        builder.append("import asyncio").append(NEW_LINE);
        builder.append("from MakerPlayground import MP, MPRunner").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream()
                .filter(projectDevice -> configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isPresent())
                .map(projectDevice -> configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).orElseThrow().getMpLibrary(project.getSelectedPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(parseImportStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    private void appendBeginFunctions() {
        project.getBegin().forEach(begin -> {
            List<NodeElement> adjacentVertices = Utility.findAdjacentNodes(project, begin);
            List<Scene> adjacentScene = Utility.takeScene(adjacentVertices);
            List<Condition> adjacentCondition = Utility.takeCondition(adjacentVertices);
            List<Delay> adjacentDelay = Utility.takeDelay(adjacentVertices);

            // generate code for begin
            builder.append(NEW_LINE);
            builder.append("def ").append(parseNodeFunctionName(begin)).append("():").append(NEW_LINE);
            if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                if (adjacentScene.size() != 1) {
                    throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                }
                Scene currentScene = adjacentScene.get(0);
                builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseNodeFunctionName(currentScene)).append(NEW_LINE);
            } else if (!adjacentCondition.isEmpty() || !adjacentDelay.isEmpty()) { // there is a condition so we generate code for that condition
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
        List<Delay> adjacentDelay;
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
            adjacentDelay = Utility.takeDelay(adjacentNodes);
            nodeToTraverse.addAll(adjacentScene.stream().filter(scene -> !visitedNodes.contains(scene)).collect(Collectors.toSet()));
            nodeToTraverse.addAll(adjacentCondition.stream().filter(condition -> !visitedNodes.contains(condition)).collect(Collectors.toSet()));
            nodeToTraverse.addAll(adjacentDelay.stream().filter(delay -> !visitedNodes.contains(delay)).collect(Collectors.toSet()));

            // Generate code for node
            if (node instanceof Scene) {
                Scene currentScene = (Scene) node;
                Begin root = node.getRoot();

                // create function header
                builder.append(NEW_LINE);
                builder.append("def ").append(parseNodeFunctionName(currentScene)).append("():").append(NEW_LINE);
                builder.append(INDENT).append("MP.update()").append(NEW_LINE);
                // do action
                for (UserSetting setting : currentScene.getAllSettings()) {
                    ProjectDevice device = setting.getDevice();
                    if (Memory.projectDevice.equals(device)) {
                        if (Memory.setValue.equals(setting.getAction())) {
                            Map<Parameter, Expression> map = setting.getParameterMap();
                            Parameter nameParam = setting.getAction().getParameter().get(0);
                            String deviceName = parseExpressionForParameter(nameParam, map.get(nameParam));
                            Parameter valueParam = setting.getAction().getParameter().get(1);
                            String expr = parseExpressionForParameter(valueParam, map.get(valueParam));
                            builder.append(INDENT).append("MP.memory[\"").append(deviceName).append("\"]").append(" = ").append(expr).append(NEW_LINE);
                        } else {
                            throw new IllegalStateException();
                        }
                    } else {
                        String deviceName = parseDeviceVariableName(searchGroup(device));
                        List<String> taskParameter = new ArrayList<>();
                        List<Parameter> parameters = setting.getAction().getParameter();
                        // generate code to perform the action
                        for (Parameter p : parameters) {
                            taskParameter.add(parseExpressionForParameter(p, setting.getParameterMap().get(p)));
                        }
                        builder.append(INDENT).append(deviceName).append(".").append(setting.getAction().getFunctionName())
                                .append("(").append(String.join(", ", taskParameter)).append(")").append(NEW_LINE);
                    }
                }

                // used for time elapsed condition and delay
                builder.append(INDENT).append(parseBeginRecentSceneFinishTime(root)).append(" = time.time()").append(NEW_LINE);

                if (!adjacentScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                    if (adjacentScene.size() != 1) {
                        throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                    }
                    Scene s = adjacentScene.get(0);
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(s)).append(NEW_LINE);
                } else if (!adjacentCondition.isEmpty()) { // there is a condition so we generate code for that condition
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseConditionFunctionName(currentScene)).append(NEW_LINE);
                } else {
                    builder.append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(root)).append(NEW_LINE);
                }

                // end of scene's function
                builder.append(NEW_LINE);
            }
        }
    }

    private List<ProjectDevice> searchGroup(ProjectDevice projectDevice) {
        Optional<List<ProjectDevice>> projectDeviceOptional = projectDeviceGroup.stream().filter(projectDeviceList -> projectDeviceList.contains(projectDevice)).findFirst();
        if (projectDeviceOptional.isEmpty()) {
            throw new IllegalStateException("Device that its value is used in the project must be exists in the device group.");
        }
        return projectDeviceOptional.get();
    }

    private void appendConditionFunctions() {
        Set<NodeElement> visitedNodes = new HashSet<>();
        List<NodeElement> adjacentNodes;
        List<Scene> adjacentScene;
        List<Condition> adjacentCondition;
        List<Delay> adjacentDelay;
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
            adjacentDelay = Utility.takeDelay(adjacentNodes);
            nodeToTraverse.addAll(adjacentScene.stream().filter(scene -> !visitedNodes.contains(scene)).collect(Collectors.toList()));
            nodeToTraverse.addAll(adjacentCondition.stream().filter(condition -> !visitedNodes.contains(condition)).collect(Collectors.toList()));
            nodeToTraverse.addAll(adjacentDelay.stream().filter(delay -> !visitedNodes.contains(delay)).collect(Collectors.toSet()));

            if (!adjacentCondition.isEmpty() || !adjacentDelay.isEmpty()) { // there is a condition so we generate code for that condition
                Begin root = node.getRoot();

                builder.append(NEW_LINE);
                builder.append("def ").append(parseConditionFunctionName(node)).append("():").append(NEW_LINE);

                // call the update function
                builder.append(INDENT).append("MP.update();").append(NEW_LINE);
                // generate if for delay
                if (!adjacentDelay.isEmpty()) {
                    if (adjacentDelay.size() != 1) {
                        throw new IllegalStateException("Connecting multiple delay to the same node is not allowed");
                    }
                    Delay currentDelay = adjacentDelay.get(0);
                    double delayInSecond;
                    if (currentDelay.getDelayUnit() == DelayUnit.SECOND) {
                        delayInSecond = currentDelay.getDelayValue();
                    } else if (currentDelay.getDelayUnit() == DelayUnit.MILLISECOND) {
                        delayInSecond = currentDelay.getDelayValue() / 1000.0;
                    } else {
                        throw new IllegalStateException();
                    }
                    builder.append(INDENT).append("if time.time() > ").append(parseBeginRecentSceneFinishTime(root)).append(" + ").append(delayInSecond).append(":").append(NEW_LINE);
                    List<NodeElement> nextNodes = Utility.findAdjacentNodes(project, currentDelay);
                    List<Scene> nextScene = Utility.takeScene(nextNodes);
                    List<Condition> nextCondition = Utility.takeCondition(nextNodes);
                    List<Delay> nextDelay = Utility.takeDelay(nextNodes);

                    if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                        if (nextScene.size() != 1) {
                            throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                        }
                        Scene s = nextScene.get(0);
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(s)).append(";").append(NEW_LINE);
                    } else if (!nextCondition.isEmpty() || !nextDelay.isEmpty()) {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseConditionFunctionName(currentDelay)).append(";").append(NEW_LINE);
                    } else {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(root)).append(";").append(NEW_LINE);
                    }
                }
                // generate if for each condition
                for (Condition condition : adjacentCondition) {
                    List<String> booleanExpressions = new ArrayList<>();
                    for (UserSetting setting : condition.getVirtualDeviceSetting()) {
                        if (setting.getCondition() == null) {
                            throw new IllegalStateException("UserSetting {" + setting + "}'s condition must be set ");
                        } else if (TimeElapsed.projectDevice.equals(setting.getDevice())) {
                            Parameter valueParameter = setting.getCondition().getParameter().get(0);
                            if (setting.getCondition() == TimeElapsed.lessThan) {
                                booleanExpressions.add("time.time() < " + parseBeginRecentSceneFinishTime(root) + " + " +
                                        parseExpressionForParameter(valueParameter, setting.getParameterMap().get(valueParameter)));
                            } else if (setting.getCondition() == TimeElapsed.greaterThan) {
                                booleanExpressions.add("time.time() > " + parseBeginRecentSceneFinishTime(root) + " + " +
                                        parseExpressionForParameter(valueParameter, setting.getParameterMap().get(valueParameter)));
                            } else {
                                throw new IllegalStateException("Found unsupported user setting {" + setting + "} / condition {" + setting.getCondition() + "}");
                            }
                        } else if (Memory.projectDevice.equals(setting.getDevice())) {
                            if (Memory.compare.equals(setting.getCondition())) {
                                for (Value value : setting.getExpression().keySet()) {
                                    if (setting.getExpressionEnable().get(value)) {
                                        Expression expression = setting.getExpression().get(value);
                                        booleanExpressions.add("(" + parseTerms(expression.getTerms()) + ")");
                                    }
                                }
                            }
                        } else {
                            throw new IllegalStateException("Found unsupported user setting {" + setting + "}");
                        }
                    }
                    for (UserSetting setting : condition.getSetting()) {
                        if (setting.getCondition() == null) {
                            throw new IllegalStateException("UserSetting {" + setting + "}'s action must be set ");
                        } else if (!setting.getCondition().getName().equals("Compare")) {
                            List<String> params = new ArrayList<>();
                            setting.getCondition().getParameter().forEach(parameter -> params.add(parseExpressionForParameter(parameter, setting.getParameterMap().get(parameter))));
                            booleanExpressions.add(parseDeviceVariableName(searchGroup(setting.getDevice())) + "." +
                                    setting.getCondition().getFunctionName() + "(" + String.join(",", params) + ")");
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
                    List<Delay> nextDelay = Utility.takeDelay(nextNodes);

                    if (!nextScene.isEmpty()) { // if there is any adjacent scene, move to that scene and ignore condition (short circuit)
                        if (nextScene.size() != 1) {
                            throw new IllegalStateException("Connection to multiple scene from the same source is not allowed");
                        }
                        Scene s = nextScene.get(0);
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(s)).append(NEW_LINE);
                    } else if (!nextCondition.isEmpty() || !nextDelay.isEmpty()) {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseConditionFunctionName(condition)).append(";").append(NEW_LINE);
                    } else {
                        builder.append(INDENT).append(INDENT).append(parsePointerName(root)).append(" = ").append(parseNodeFunctionName(root)).append(NEW_LINE);
                    }
                }
                builder.append(NEW_LINE); // end of while loop
            }
        }
    }

    private void appendGenMessageFunction() {
        builder.append(NEW_LINE);
        builder.append("def gen_message():").append(NEW_LINE);
        builder.append(INDENT).append("retval = ''").append(NEW_LINE);

        Map<ProjectDevice, Set<Value>> valueUsed = project.getAllValueUsedMap(EnumSet.of(DataType.DOUBLE, DataType.INTEGER));
        for (ProjectDevice projectDevice : valueUsed.keySet()) {
            if (!valueUsed.get(projectDevice).isEmpty()) {
                builder.append(INDENT).append("retval += f'[[V]] ")
                        .append("\"").append(projectDevice.getName()).append("\" ")
                        .append(valueUsed.get(projectDevice).stream()
                                .map(value -> "\"" + value.getName() + "\"={" + parseValueVariableTerm(searchGroup(projectDevice), value) + "}")
                                .collect(Collectors.joining(",")))
                        .append("\\n'")
                        .append(NEW_LINE);
            }
        }
        builder.append(INDENT).append("return retval").append(NEW_LINE);
        builder.append(NEW_LINE);

    }

    private void appendMainCode() {
        builder.append(NEW_LINE);
        builder.append("async def main():").append(NEW_LINE);
        builder.append(INDENT).append("try:").append(NEW_LINE);

        /* Setup */
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
        builder.append(NEW_LINE);
        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            if (projectDeviceList.isEmpty()) {
                throw new IllegalStateException();
            }
            Optional<ActualDevice> actualDeviceOptional = configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDeviceList.get(0));
            if (actualDeviceOptional.isEmpty()) {
                throw new IllegalStateException();
            }
            ActualDevice actualDevice = actualDeviceOptional.get();

            List<String> args = new ArrayList<>();

            DeviceConnection connection = project.getProjectConfiguration().getDeviceConnection(projectDeviceList.get(0));
            if (connection != DeviceConnection.NOT_CONNECTED) {
                Map<Connection, Connection> connectionMap = connection.getConsumerProviderConnections();
                for (Connection connectionConsume: actualDevice.getConnectionConsumeByOwnerDevice(projectDeviceList.get(0))) {
                    Connection connectionProvide = connectionMap.get(connectionConsume);
                    for (int i=connectionConsume.getPins().size()-1; i>=0; i--) {
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
            for (Property p : actualDevice.getProperty()) {
                ProjectDevice projectDevice = configuration.getRootDevice(projectDeviceList.get(0));
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

//            // Cloud Platform instance
//            CloudPlatform cloudPlatform = actualDevice.getCloudConsume();
//            if (cloudPlatform != null) {
//                args.add(parseCloudPlatformVariableName(cloudPlatform));
//            }

            builder.append(INDENT).append(INDENT).append(parseDeviceVariableName(projectDeviceList))
                    .append(" = ").append(actualDevice.getMpLibrary(project.getSelectedPlatform()))
                    .append("(").append(String.join(", ", args)).append(")").append(NEW_LINE);
        }
        // TODO: We should declare only the variables used
        Memory.unmodifiableVariables.forEach(projectValue -> {
            if (projectValue.getValue().getType() == DataType.DOUBLE) {
                builder.append(INDENT).append(INDENT).append("MP.memory[\"").append(projectValue.getValue().getName()).append("\"] = 0.0").append(NEW_LINE);
            }
        });
        builder.append(NEW_LINE);
        project.getBegin().forEach(begin -> builder.append(INDENT).append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseNodeFunctionName(begin)).append(NEW_LINE));
        /* End Setup */

        /* Loop */
        builder.append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("while True:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("MP.update()").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("await asyncio.sleep(0)").append(NEW_LINE);
        project.getBegin().forEach(begin -> builder.append(INDENT).append(INDENT).append(INDENT).append(parsePointerName(begin)).append("()").append(NEW_LINE));
        /* End Loop */

        /* Handle Interrupt */
        builder.append(INDENT).append("except KeyboardInterrupt:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("MP.cleanup()").append(NEW_LINE);
        /* End Handle Interrupt */

        builder.append("if __name__ == '__main__':").append(NEW_LINE);
        builder.append(INDENT).append("MPRunner.start(main, gen_message)").append(NEW_LINE);
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
            if (Memory.projectDevice.equals(value.getDevice())) {
                return "MP.memory[\"" + value.getValue().getName() + "\"]";
            }
            return parseValueVariableTerm(searchGroup(value.getDevice()), value.getValue());
        } else if (term instanceof IntegerTerm) {
            return term.toString();
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
            returnValue = "MP.constrain(MP.map(" + parseValueVariableTerm(searchGroup(valueLinkingExpression.getSourceValue().getDevice())
                    , valueLinkingExpression.getSourceValue().getValue()) + ", " + fromLow + ", " + fromHigh
                    + ", " + toLow + ", " + toHigh + "), " + toLow + ", " + toHigh + ")";
        } else if (expression instanceof ProjectValueExpression) {
            ProjectValueExpression projectValueExpression = (ProjectValueExpression) expression;
            ProjectValue projectValue = projectValueExpression.getProjectValue();
            DataType dataType = projectValue.getValue().getType();
            if (dataType == DataType.STRING) {
                returnValue = parseValueVariableTerm(searchGroup(projectValue.getDevice()), projectValue.getValue());
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
            returnValue = parseValueVariableTerm(searchGroup(projectValue.getDevice()), projectValue.getValue());
        } else if (expression instanceof ComplexStringExpression) {
            List<Expression> subExpression = ((ComplexStringExpression) expression).getSubExpressions();
            if (subExpression.size() == 1 && subExpression.get(0) instanceof SimpleStringExpression) {  // only one string, generate normal C string
                returnValue = "'" + ((SimpleStringExpression) subExpression.get(0)).getString() + "'";
            } else if (subExpression.size() == 1 && subExpression.get(0) instanceof CustomNumberExpression) {  // only one number expression
                returnValue = "str(" + parseTerms(subExpression.get(0).getTerms()) + ")";
            } else if (subExpression.stream().allMatch(e -> e instanceof SimpleStringExpression)) {     // every expression is a string so we join them
                returnValue = subExpression.stream().map(e -> ((SimpleStringExpression) e).getString())
                        .collect(Collectors.joining("", "'", "'"));
            } else {
                List<String> subExpressionString = new ArrayList<>();
                for (Expression e : subExpression) {
                    if (e instanceof SimpleStringExpression) {
                        subExpressionString.add("'" + ((SimpleStringExpression) e).getString() + "'");
                    } else if (e instanceof CustomNumberExpression) {
                        subExpressionString.add("str(" + parseTerms(e.getTerms()) + ")");
                    } else {
                        throw new IllegalStateException(e.getClass().getName() + " is not supported in ComplexStringExpression");
                    }
                }
                returnValue = "(" + String.join("+", subExpressionString) + ")";
            }
        } else if (expression instanceof SimpleIntegerExpression) {
            returnValue = ((SimpleIntegerExpression) expression).getInteger().toString();
        } else if (expression instanceof VariableExpression) {
            returnValue = ((VariableExpression) expression).getVariableName();
        } else {
            throw new IllegalStateException();
        }
        return returnValue;
    }
}
