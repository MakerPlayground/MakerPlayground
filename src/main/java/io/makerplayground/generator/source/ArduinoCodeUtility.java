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
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.util.*;
import java.util.stream.Collectors;

class ArduinoCodeUtility {

    static final String INDENT = "    ";
    static final String NEW_LINE = "\n";

    static final Set<PinFunction> PIN_FUNCTION_WITH_CODES = Set.of(
            PinFunction.DIGITAL_IN, PinFunction.DIGITAL_OUT,
            PinFunction.ANALOG_IN, PinFunction.ANALOG_OUT,
            PinFunction.PWM_OUT,
            PinFunction.INTERRUPT_LOW, PinFunction.INTERRUPT_HIGH, PinFunction.INTERRUPT_CHANGE, PinFunction.INTERRUPT_RISING, PinFunction.INTERRUPT_FALLING,
            PinFunction.HW_SERIAL_RX, PinFunction.HW_SERIAL_TX, PinFunction.SW_SERIAL_RX, PinFunction.SW_SERIAL_TX
    );

    static String getSetupFunctionCode(Project project, List<List<ProjectDevice>> projectDeviceGroup, boolean withBegin) {
        StringBuilder builder = new StringBuilder();
        builder.append("void setup() {").append(NEW_LINE);
        builder.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);

        if (project.getSelectedPlatform().equals(Platform.ARDUINO_ESP32)) {
            builder.append(INDENT).append("analogSetWidth(10);").append(NEW_LINE);
        }

        if (!project.getProjectConfiguration().useHwSerialProperty().get()) {
            for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
                String cloudPlatformVariableName = parseCloudPlatformVariableName(cloudPlatform);
                builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append("->init();").append(NEW_LINE);
                builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(cloudPlatform.getDisplayName()).append("\", status_code);").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
                builder.append(INDENT).append("}").append(NEW_LINE);
                builder.append(NEW_LINE);
            }

            for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
                String variableName = parseDeviceVariableName(projectDeviceList);
                builder.append(INDENT).append("status_code = ").append(variableName).append(".init();").append(NEW_LINE);
                builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(projectDeviceList.stream().map(ProjectDevice::getName).collect(Collectors.joining(", "))).append("\", status_code);").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
                builder.append(INDENT).append("}").append(NEW_LINE);
                builder.append(NEW_LINE);
            }
        }
        if (withBegin) {
            project.getBegin().forEach(begin -> builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(ArduinoCodeUtility.parseNodeFunctionName(begin)).append(";").append(NEW_LINE));
        }
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
        return builder.toString();
    }

    static String getInstanceVariablesCode(Project project, List<List<ProjectDevice>> projectDeviceGroup) {
        StringBuilder builder = new StringBuilder();
        ProjectConfiguration configuration = project.getProjectConfiguration();
        // create cloud singleton variables
        for (CloudPlatform cloudPlatform: project.getAllCloudPlatforms()) {
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
                                if (ArduinoCodeUtility.PIN_FUNCTION_WITH_CODES.contains(function)) {
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
        return builder.toString();
    }

    static String parseDeviceVariableName(List<ProjectDevice> projectDeviceList) {
        if (projectDeviceList.isEmpty()) {
            throw new IllegalStateException("Cannot get device name if there is no devices");
        }
        return "_" + projectDeviceList.stream().map(ProjectDevice::getName).map(s -> s.replace(' ', '_')).collect(Collectors.joining("_"));
    }

    static String parseValueVariableTerm(List<ProjectDevice> projectDeviceList, Value value) {
        return parseDeviceVariableName(projectDeviceList) + ".get" + value.getName().replace(' ', '_').replace(".", "_") + "()";
    }

    static String parseCloudPlatformVariableName(CloudPlatform cloudPlatform) {
        return "_" + cloudPlatform.getLibName().replace(' ', '_');
    }

    static String parseIncludeStatement(String libName) {
        return "#include \"" + libName + ".h\"";
    }

    static String parseNodeFunctionName(NodeElement node) {
        if (node instanceof Scene) {
            return "scene_" + node.getNameSanitized();
        } else if (node instanceof Begin) {
            return "begin_" + node.getNameSanitized();
        } else if (node instanceof Delay) {
            return "delay_" + node.getNameSanitized();
        } else if (node instanceof Condition) {
            return "condition_" + node.getNameSanitized();
        }
        throw new IllegalStateException("Not support scene function displayName for {" + node + "}");
    }

    static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "current_" + nodeElement.getNameSanitized();
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

    static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
        if (nodeBeforeConditions instanceof Begin || nodeBeforeConditions instanceof Scene || nodeBeforeConditions instanceof Delay || nodeBeforeConditions instanceof Condition) {
            return parseNodeFunctionName(nodeBeforeConditions) + "_options";
        }
        throw new IllegalStateException("Not support condition function displayName for {" + nodeBeforeConditions + "}");
    }
}
