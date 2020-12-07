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
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.util.*;
import java.util.stream.Collectors;

class MicroPythonCodeUtility extends ArduinoCodeUtility {
    static String parseIncludeStatement(String libName) {
        return "from " + libName + " import *";
    }

    static String getSetupFunctionCode(Project project, List<List<ProjectDevice>> projectDeviceGroup, boolean withBegin, boolean isInteractive) {
        StringBuilder builder = new StringBuilder();
        builder.append("def setup():").append(NEW_LINE);

        if (!project.getProjectConfiguration().useHwSerialProperty().get()) {
            Set<CloudPlatform> cloudPlatforms = isInteractive ? project.getAllCloudPlatforms() : project.getCloudPlatformUsed();
            for (CloudPlatform cloudPlatform : cloudPlatforms) {
                String cloudPlatformVariableName = parseCloudPlatformVariableName(cloudPlatform);
                builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append(".init()").append(NEW_LINE);
                builder.append(INDENT).append("if status_code != 0:").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("mp.ERR(\"").append(cloudPlatform.getDisplayName()).append("\", status_code)").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("sys.exit(1)").append(NEW_LINE);
                builder.append(NEW_LINE);
            }

            for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
                String variableName = parseDeviceVariableName(projectDeviceList);
                builder.append(INDENT).append("status_code = ").append(variableName).append(".init()").append(NEW_LINE);
                builder.append(INDENT).append("if status_code != 0:").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("mp.MP_ERR(\"").append(projectDeviceList.stream().map(ProjectDevice::getName).collect(Collectors.joining(", "))).append("\", status_code)").append(NEW_LINE);
                builder.append(INDENT).append(INDENT).append("sys.exit(1)").append(NEW_LINE);
                builder.append(NEW_LINE);
            }
        }
        if (withBegin) {
            project.getBegin().forEach(begin -> {
                builder.append(INDENT).append("global ").append(parsePointerName(begin)).append(NEW_LINE);
                builder.append(INDENT).append(parsePointerName(begin)).append(" = ").append(parseNodeFunctionName(begin)).append(NEW_LINE);
            });
        }
        builder.append(NEW_LINE);
        return builder.toString();
    }

    static String getInstanceVariablesCode(Project project, List<List<ProjectDevice>> projectDeviceGroups, boolean isInteractive) {
        StringBuilder builder = new StringBuilder();
        ProjectConfiguration configuration = project.getProjectConfiguration();
        // create cloud singleton variables
        Set<CloudPlatform> cloudPlatforms = isInteractive ? project.getAllCloudPlatforms() : project.getCloudPlatformUsed();
        for (CloudPlatform cloudPlatform: cloudPlatforms) {
            String cloudPlatformLibName = cloudPlatform.getLibName();
            String specificCloudPlatformLibName = project.getSelectedController().getCloudPlatformSourceCodeLibrary().get(cloudPlatform).getClassName();

            List<String> cloudPlatformParameterValues = cloudPlatform.getParameter().stream()
                    .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
            builder.append(parseCloudPlatformVariableName(cloudPlatform)).append(" = ").append(specificCloudPlatformLibName)
                    .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(")").append(NEW_LINE);
        }

        for (List<ProjectDevice> group: projectDeviceGroups) {
            if (group.isEmpty()) {
                throw new IllegalStateException();
            }
            Optional<ActualDevice> actualDeviceOptional = configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(group.get(0));
            if (actualDeviceOptional.isEmpty()) {
                throw new IllegalStateException();
            }
            ActualDevice actualDevice = actualDeviceOptional.get();
            builder.append(parseDeviceVariableName(group)).append(" = ").append(actualDevice.getMpLibrary(project.getSelectedPlatform()));
            List<String> args = new ArrayList<>();

            DeviceConnection connection = project.getProjectConfiguration().getDeviceOrIdenticalDeviceConnection(group.get(0));
            if (connection != DeviceConnection.NOT_CONNECTED) {
                Map<Connection, Connection> connectionMap = connection.getConsumerProviderConnections();
                for (Connection connectionConsume: actualDevice.getConnectionConsumeByOwnerDevice(group.get(0))) {
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
                ProjectDevice projectDevice = configuration.getRootDevice(group.get(0));
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
                    case STRING_INT_ENUM:
                        Map<String, Integer> map = ((StringIntegerCategoricalConstraint) p.getConstraint()).getMap();
                        args.add(String.valueOf(map.get(value)));
                        break;
                    default:
                        throw new IllegalStateException("Property (" + value + ") hasn't been supported yet");
                }
            }

            // Cloud Platform instance
            CloudPlatform cloudPlatform = actualDevice.getCloudConsume();
            if (cloudPlatform != null) {
                args.add(parseCloudPlatformVariableName(cloudPlatform));
            }

            if (!args.isEmpty()) {
                builder.append("(").append(String.join(", ", args)).append(")").append(NEW_LINE);
            } else {
                builder.append("()").append(NEW_LINE);
            }
        }

        // TODO: We should declare only the variables used
        project.getUnmodifiableVariable().forEach(projectValue -> {
            if (projectValue.getValue().getType() == DataType.DOUBLE) {
                builder.append(projectValue.getValue().getName()).append(" = 0.0").append(NEW_LINE);
            }
        });

        builder.append(NEW_LINE);
        return builder.toString();
    }

}
