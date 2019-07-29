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

package io.makerplayground.generator.devicemapping;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.PinPortConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.expression.*;

import java.util.function.Function;

public class ProjectLogic {

    public static Constraint extractConstraint(Parameter parameter, Expression expression) {
        Constraint newConstraint = Constraint.NONE;
        switch (parameter.getDataType()) {
            case DOUBLE:
            case INTEGER:
                if (expression instanceof NumberWithUnitExpression) {
                    NumberWithUnit n = ((NumberWithUnitExpression) expression).getNumberWithUnit();
                    newConstraint = Constraint.createNumericConstraint(n.getValue(), n.getValue(), n.getUnit());
                } else if (expression instanceof CustomNumberExpression) {
                    // TODO: should be calculated from the expression or use range of parameter value
                } else if (expression instanceof ValueLinkingExpression) {
                    ValueLinkingExpression exp = (ValueLinkingExpression) expression;
                    newConstraint = Constraint.createNumericConstraint(exp.getDestinationLowValue().getValue(), exp.getDestinationHighValue().getValue(), exp.getDestinationLowValue().getUnit());
                } else if (expression instanceof ProjectValueExpression) {
                    ProjectValueExpression exp = (ProjectValueExpression) expression;
                    if (exp.getProjectValue() != null) {
                        newConstraint = ((NumericConstraint) parameter.getConstraint()).intersect(exp.getProjectValue().getValue().getConstraint(), Function.identity());
                    }
                } else {
                    throw new IllegalStateException("Constraint is not defined for expression type: " + expression.getClass().getCanonicalName());
                }
                break;
            case BOOLEAN_ENUM:
            case INTEGER_ENUM:
            case STRING:
            case ENUM:
                if (expression instanceof SimpleStringExpression) {
                    newConstraint = Constraint.createCategoricalConstraint(((SimpleStringExpression) expression).getString());
                }
                break;

            case DATETIME:
            case RECORD:
            case IMAGE:
                newConstraint = Constraint.NONE;
                break;

            case AZURE_COGNITIVE_KEY:
            case AZURE_IOTHUB_KEY:
            default:
                throw new IllegalStateException("There isn't any method to calculate constraint from this parameter's data type");
        }
        return newConstraint;
    }

    public static ProjectMappingResult validateDeviceAssignment(Project project) {
        ProjectConfiguration configuration = project.getProjectConfiguration();
        if (configuration.getController() == null) {
            return ProjectMappingResult.NO_MCU_SELECTED;
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (configuration.getActualDevice(projectDevice).isEmpty() && configuration.getIdenticalDevice(projectDevice).isEmpty()) {
                return ProjectMappingResult.NOT_SELECT_DEVICE;
            }

            // for each connectivity required, check if it has been connected and indicate error if it hasn't
            if (configuration.getActualDevice(projectDevice).isPresent()) {
                ProjectDevice root = configuration.getIdenticalDevice(projectDevice).orElse(projectDevice);
                var projectDeviceConnectionMap = project.getProjectConfiguration().getUnmodifiableDevicePinPortConnections();
                if (!projectDeviceConnectionMap.containsKey(root) || projectDeviceConnectionMap.get(root) == PinPortConnection.NOT_CONNECTED) {
                    return ProjectMappingResult.NOT_SELECT_PORT;
                }
            }
        }

        return ProjectMappingResult.OK;
    }
//
//    public static ProjectMappingResult autoAssignDevices(Project project) {
//        if (project.getSelectedController() == null) {
//            return ProjectMappingResult.NO_MCU_SELECTED;
//        }
//
//        // reclaim ports from unused devices
//        for (ProjectDevice projectDevice : project.getAllDeviceUnused()) {
//            projectDevice.unsetDeviceConnection();
//        }
//
//        Map<ProjectDevice, List<ActualDevice>> supportedDeviceMap = getSupportedDeviceList(project);
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            if (supportedDeviceMap.get(projectDevice).isEmpty()) {
//                return ProjectMappingResult.NO_SUPPORT_DEVICE;
//            }
//
//            boolean done = true;
//            if (projectDevice.isMergeToOtherDevice()) {
//                // skip
//            } else if (projectDevice.isActualDevicePresent()) {
//                // try assign port based on the device that user has selected
//                done = assignPort(project, projectDevice);
//            } else {
//                // try each support device until we find the one that works
//                for (ActualDevice actualDevice : supportedDeviceMap.get(projectDevice)) {
//                    projectDevice.unsetDeviceConnection();
//                    projectDevice.setActualDevice(actualDevice);
//                    done = assignPort(project, projectDevice);
//                    if (done) {
//                        break;
//                    }
//                }
//            }
//            if (!done) {
//                return ProjectMappingResult.CANT_ASSIGN_PORT;
//            }
//        }
//        return ProjectMappingResult.OK;
//    }
//
//    private static boolean assignPort(Project project, ProjectDevice projectDevice) {
//        for (Peripheral devicePeripheral : projectDevice.getCompatibleDeviceComboItem().getConnectivity()) {
//            if (!projectDevice.getDeviceConnection().containsKey(devicePeripheral)) {
//                List<List<DevicePort>> port = getDeviceCompatiblePort(project).get(projectDevice).get(devicePeripheral);
//                if (port.isEmpty()) {
//                    return false;
//                }
//
//                if (devicePeripheral.isI2C()) {
//                    // I2C port of Maker Playground, Grove and INEX platform can be shared by using an external
//                    // hub so we try to assign to a free port first before forcing user to add a hub
//                    List<DevicePort> unusedPort = port.stream().filter(dp -> !hasPortUsed(project, dp))
//                            .findFirst().orElse(port.get(0));
//                    projectDevice.setDeviceConnection(devicePeripheral, unusedPort);
//                } else {
//                    projectDevice.setDeviceConnection(devicePeripheral, port.get(0));
//                }
//            }
//        }
//        return true;
//    }
//
//    private static boolean hasPortUsed(Project project, List<DevicePort> portList) {
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            List<DevicePort> portUsed = projectDevice.getDeviceConnection().values().stream()
//                    .flatMap(Collection::stream).collect(Collectors.toList());
//            for (DevicePort port : portList) {
//                if (portUsed.contains(port)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    public ActualDeviceComboItem getControllerComboItem(ActualDevice controller) {
//        return controllerComboItemList.stream()
//                .filter(item -> item.getCompatibleDeviceComboItem() == controller)
//                .findFirst()
//                .orElseThrow();
//    }


//    public ActualDeviceComboItem getSelectedControllerComboItem() {
//        return controllerComboItemList.stream()
//                .filter(actualDeviceComboItem -> actualDeviceComboItem.getCompatibleDeviceComboItem() == project.getSelectedController())
//                .findFirst()
//                .orElseThrow();
//    }

}