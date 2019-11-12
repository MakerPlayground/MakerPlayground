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
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.*;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InteractiveArduinoCodeGenerator {

    static final String INDENT = "    ";
    static final String NEW_LINE = "\n";

    final Project project;
    final ProjectConfiguration configuration;
    final StringBuilder builder = new StringBuilder();
    private final List<Scene> allSceneUsed;
    private final List<Condition> allConditionUsed;
    private final List<List<ProjectDevice>> projectDeviceGroup;

    private static final Set<PinFunction> PIN_FUNCTION_WITH_CODES = Set.of(
            PinFunction.DIGITAL_IN, PinFunction.DIGITAL_OUT,
            PinFunction.ANALOG_IN, PinFunction.ANALOG_OUT,
            PinFunction.PWM_OUT,
            PinFunction.INTERRUPT_LOW, PinFunction.INTERRUPT_HIGH, PinFunction.INTERRUPT_CHANGE, PinFunction.INTERRUPT_RISING, PinFunction.INTERRUPT_FALLING,
            PinFunction.HW_SERIAL_RX, PinFunction.HW_SERIAL_TX, PinFunction.SW_SERIAL_RX, PinFunction.SW_SERIAL_TX
    );

    private InteractiveArduinoCodeGenerator(Project project) {
        this.project = project;
        this.configuration = project.getProjectConfiguration();
        Set<NodeElement> allNodeUsed = Utility.getAllUsedNodes(project);
        this.allSceneUsed = Utility.takeScene(allNodeUsed);
        this.allConditionUsed = Utility.takeCondition(allNodeUsed);
        this.projectDeviceGroup = project.getAllDevicesGroupBySameActualDevice();
    }

    static SourceCodeResult generateCode(Project project) {
        // Check if all used devices are assigned.
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }

        InteractiveArduinoCodeGenerator generator = new InteractiveArduinoCodeGenerator(project);
        generator.appendHeader(project.getUnmodifiableProjectDevice(), project.getAllCloudPlatforms());
        generator.appendGlobalVariable();
        generator.appendInstanceVariables(project.getAllCloudPlatforms());
        generator.appendSetupFunction();
        generator.appendProcessCommand();
        generator.appendLoopFunction();
//        System.out.println(generator.builder.toString());
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendHeader(Collection<ProjectDevice> devices, Collection<CloudPlatform> cloudPlatforms) {
        builder.append("#include \"MakerPlayground.h\"").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = devices.stream()
                .filter(projectDevice -> configuration.getActualDevice(projectDevice).isPresent())
                .map(projectDevice -> configuration.getActualDevice(projectDevice).orElseThrow().getMpLibrary(project.getSelectedPlatform()));
        Stream<String> cloud_libs = cloudPlatforms.stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(ArduinoCodeUtility.parseIncludeStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    private void appendGlobalVariable() {
        builder.append("uint8_t statusCode = 0;").append(NEW_LINE);
        builder.append("unsigned long lastSendTime = 0;").append(NEW_LINE);
//        builder.append("unsigned long currentTime = 0;").append(NEW_LINE);
        builder.append("const int SEND_INTERVAL = 100;").append(NEW_LINE);
        builder.append("char serialBuffer[256];").append(NEW_LINE);
        builder.append("uint8_t serialBufferIndex = 0;").append(NEW_LINE);
        builder.append("char* commandArgs[10];").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendInstanceVariables(Collection<CloudPlatform> cloudPlatforms) {
        // create cloud singleton variables
        for (CloudPlatform cloudPlatform: cloudPlatforms) {
            String cloudPlatformLibName = cloudPlatform.getLibName();
            String specificCloudPlatformLibName = project.getSelectedController().getCloudPlatformSourceCodeLibrary().get(cloudPlatform).getClassName();

            List<String> cloudPlatformParameterValues = cloudPlatform.getParameter().stream()
                    .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
            builder.append(cloudPlatformLibName).append("* ").append(ArduinoCodeUtility.parseCloudPlatformVariableName(cloudPlatform))
                    .append(" = new ").append(specificCloudPlatformLibName)
                    .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
        }

        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            if (projectDeviceList.isEmpty()) {
                throw new IllegalStateException();
            }
            Optional<ActualDevice> actualDeviceOptional = configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDeviceList.get(0));
            if (actualDeviceOptional.isEmpty()) {
                throw new IllegalStateException();
            }
            ActualDevice actualDevice = actualDeviceOptional.get();
            builder.append(actualDevice.getMpLibrary(project.getSelectedPlatform()))
                    .append(" ").append(ArduinoCodeUtility.parseDeviceVariableName(projectDeviceList));
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

            // Cloud Platform instance
            CloudPlatform cloudPlatform = actualDevice.getCloudConsume();
            if (cloudPlatform != null) {
                args.add(ArduinoCodeUtility.parseCloudPlatformVariableName(cloudPlatform));
            }

            if (!args.isEmpty()) {
                builder.append("(").append(String.join(", ", args)).append(");").append(NEW_LINE);
            } else {
                builder.append(";").append(NEW_LINE);
            }
        }
        builder.append(NEW_LINE);
    }

    private void appendSetupFunction() {
        builder.append("void setup() {").append(NEW_LINE);
        builder.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);

        if (project.getSelectedPlatform().equals(Platform.ARDUINO_ESP32)) {
            builder.append(INDENT).append("analogSetWidth(10);").append(NEW_LINE);
        }

        for (CloudPlatform cloudPlatform : project.getAllCloudPlatforms()) {
            String cloudPlatformVariableName = ArduinoCodeUtility.parseCloudPlatformVariableName(cloudPlatform);
            builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append("->init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(cloudPlatform.getDisplayName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }

        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            String variableName = ArduinoCodeUtility.parseDeviceVariableName(projectDeviceList);
            builder.append(INDENT).append("status_code = ").append(variableName).append(".init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(projectDeviceList.stream().map(ProjectDevice::getName).collect(Collectors.joining(", "))).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendProcessCommand() {
        builder.append("void processCommand() {").append(NEW_LINE);
        builder.append(INDENT).append("uint8_t i = 0, argsCount = 0, length = strlen(serialBuffer);").append(NEW_LINE);
        builder.append(INDENT).append("while (i < length) {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("if (serialBuffer[i] == '\"') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("commandArgs[argsCount] = &serialBuffer[i];").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("argsCount++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("while (i < length && serialBuffer[i] != '\"') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[i] = '\\0';").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);
        builder.append(NEW_LINE);

        boolean firstCondition = true;
        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            if (projectDevice.getGenericDevice().hasAction()) {
                String variableName = ArduinoCodeUtility.parseDeviceVariableName(searchGroup(projectDevice));
                builder.append(INDENT).append(firstCondition ? "if " : "else if ").append("(strcmp_P(commandArgs[0], (PGM_P) F(\"")
                        .append(projectDevice.getName()).append("\")) == 0) {").append(NEW_LINE);
                firstCondition = false;

                if (project.getProjectConfiguration().getActualDevice(projectDevice).isPresent()) {
                    ActualDevice actualDevice = project.getProjectConfiguration().getActualDevice(projectDevice).get();
                    for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                        Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);

                        AtomicInteger j= new AtomicInteger();
                        compatibility.getDeviceAction().forEach((action, parameterConstraintMap) -> {
                            builder.append(j.getAndIncrement() == 0 ? INDENT + INDENT + "if " : "else if ").append("(strcmp_P(commandArgs[1], (PGM_P) F(\"")
                                    .append(action.getName()).append("\")) == 0 && argsCount == ").append(action.getParameter().size() + 2)
                                    .append(") {").append(NEW_LINE);

                            if (action.getParameter().size() == 1 && action.getParameter().get(0).getDataType() == DataType.RECORD) {
                                builder.append(INDENT).append(INDENT).append(INDENT).append("Record rec;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("uint8_t j = 0, keyValueCount = 0, length_rec = strlen(commandArgs[2]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("char* keyValues[20];").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && keyValueCount < 20) {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("if (commandArgs[2][j++] == '[') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("keyValues[keyValueCount++] = &(commandArgs[2][j]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && commandArgs[2][j] != ',') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("j++;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("commandArgs[2][j++] = '\\0';").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("keyValues[keyValueCount++] = &(commandArgs[2][j]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && commandArgs[2][j] != ']') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("j++;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("commandArgs[2][j++] = '\\0';").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("rec.put(keyValues[keyValueCount-2], atof(keyValues[keyValueCount-1]));").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName()).append("(rec);").append(NEW_LINE);
                            } else {
                                List<String> taskParameter = new ArrayList<>();
                                for (int i=0; i<action.getParameter().size(); i++) {
                                    Parameter parameter = action.getParameter().get(i);
                                    switch (parameter.getDataType()) {
                                        case DOUBLE:
                                            taskParameter.add("atof(commandArgs[" + (i+2) + "])");
                                            break;
                                        case INTEGER:
                                            taskParameter.add("atoi(commandArgs[" + (i+2) + "])");
                                            break;
                                        default:
                                            taskParameter.add("commandArgs[" + (i+2) + "]");
                                            break;
                                    }

                                }
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName())
                                        .append("(").append(String.join(", ", taskParameter)).append(");").append(NEW_LINE);
                            }

                            builder.append(INDENT).append(INDENT).append("} ");
                        });
                    }
                }

                builder.append(NEW_LINE).append(INDENT).append("}").append(NEW_LINE);
            }
        }
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendLoopFunction() {
        builder.append("void loop() {").append(NEW_LINE);

        builder.append(INDENT).append("currentTime = millis();").append(NEW_LINE);
        builder.append(NEW_LINE);

        // allow all cloud platform maintains their own tasks (e.g. connection)
        for (CloudPlatform cloudPlatform : project.getAllCloudPlatforms()) {
            builder.append(INDENT).append(ArduinoCodeUtility.parseCloudPlatformVariableName(cloudPlatform)).append("->update(currentTime);").append(NEW_LINE);
        }

        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            String variableName = ArduinoCodeUtility.parseDeviceVariableName(projectDeviceList);
            builder.append(INDENT).append(variableName).append(".update(currentTime);").append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        if (!project.getUnmodifiableProjectDevice().isEmpty()) {
            builder.append(INDENT).append("if (currentTime - lastSendTime >= SEND_INTERVAL) {").append(NEW_LINE);
            for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
                for (ProjectDevice projectDevice: projectDeviceList) {
                    if (project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isPresent()) {
                        ActualDevice actualDevice = project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).get();
                        for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                            if (genericDevice == projectDevice.getGenericDevice()) {
                                Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);
                                if (!compatibility.getDeviceCondition().isEmpty() || !compatibility.getDeviceValue().isEmpty()) {
                                    String variableName = ArduinoCodeUtility.parseDeviceVariableName(projectDeviceList);
                                    builder.append(INDENT).append(INDENT).append("Serial.print(F(\"").append(projectDevice.getName()).append("\"));").append(NEW_LINE);
                                    // condition
                                    compatibility.getDeviceCondition().forEach((condition, parameterConstraintMap) -> {
                                        if (condition.getName().equals("Compare")) {    // TODO: compare with name is dangerous
                                            return;
                                        }
                                        builder.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                                        builder.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".").append(condition.getFunctionName()).append("());").append(NEW_LINE);
                                    });
                                    // value
                                    compatibility.getDeviceValue().forEach((value, constraint) -> {
                                        builder.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                                        builder.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".get").append(value.getName()).append("());").append(NEW_LINE);
                                    });
                                    builder.append(INDENT).append(INDENT).append("Serial.println();").append(NEW_LINE);
                                    builder.append(NEW_LINE);
                                }
                            }
                        }
                    }
                }
            }
            builder.append(INDENT).append(INDENT).append("lastSendTime = millis();").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
        }

        builder.append(INDENT).append("while (Serial.available() > 0) {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = Serial.read();").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("if (serialBuffer[serialBufferIndex] == '\\r' || serialBuffer[serialBufferIndex] == '\\n') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = '\\0';").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("processCommand();").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBufferIndex = 0;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("serialBufferIndex++;").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);

        builder.append("}").append(NEW_LINE);
    }

    private List<ProjectDevice> searchGroup(ProjectDevice projectDevice) {
        Optional<List<ProjectDevice>> projectDeviceOptional = projectDeviceGroup.stream().filter(projectDeviceList -> projectDeviceList.contains(projectDevice)).findFirst();
        if (projectDeviceOptional.isEmpty()) {
            throw new IllegalStateException("Device that its value is used in the project must be exists in the device group.");
        }
        return projectDeviceOptional.get();
    }
}