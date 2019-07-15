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

import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.expression.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class ProjectConfigurationLogic {

    public static ProjectConfiguration changeCloudPlatformParameter(ProjectConfiguration conf, CloudPlatform cloudPlatform, String parameterName, String value) {
        var parameter = new TreeMap<>(conf.getUnmodifiableCloudParameterMap());
        if (parameter.containsKey(cloudPlatform)) {
            parameter.get(cloudPlatform).put(parameterName, value);
        } else {
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put(parameterName, value);
            parameter.put(cloudPlatform, parameterMap);
        }
        return ProjectConfiguration.builder()
                .platform(conf.getPlatform())
                .controller(conf.getController())
                .deviceMap(conf.getUnmodifiableDeviceMap())
                .sameDeviceMap(conf.getUnmodifiableSameDeviceMap())
                .devicePinPortConnections(conf.getUnmodifiableDevicePinPortConnections())
                .devicePropertyValueMap(conf.getUnmodifiableDevicePropertyValueMap())
                .cloudPlatformParameterMap(parameter)
                .build();
    }

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

//    public static Map<ProjectDevice, List<ActualDevice>> getSupportedDeviceList(Project project) {
//        Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> tempMap = getConstraintMap(project);
//
//        // Print to see result
////        for (ProjectDevice device : tempMap.keySet()) {
////            System.out.println(device.getName());
////            for (Action action : tempMap.get(device).keySet()) {
////                System.out.println(action.getName());
////                for (Parameter parameter : tempMap.get(device).get(action).keySet()) {
////                    System.out.println(parameter.getName() + tempMap.get(device).get(action).get(parameter));
////                }
////            }
////        }
//
//        List<ActualDevice> actualDevice = new ArrayList<>(DeviceLibrary.INSTANCE.getActualDevice(project.getSelectedPlatform()));
//        // append with integrated device of the current controller if existed
//        if (project.getSelectedController() != null) {
//            actualDevice.addAll(project.getSelectedController().getIntegratedDevices());
//        }
//
//        // Get the list of compatible device
//        Map<ProjectDevice, List<ActualDevice>> selectableDevice = new HashMap<>();
//        for (ProjectDevice device : project.getUnmodifiableProjectDevice()) {
//            selectableDevice.put(device, new ArrayList<>());
//        }
//
//        if (project.getSelectedController() != null) {
//            for (ProjectDevice device : tempMap.keySet()) {
//                for (ActualDevice d : actualDevice) {
//                    if (d.isSupport(project.getSelectedController(), device.getGenericDevice(), tempMap.get(device))) {
//                        if (d.getCloudPlatform() != null && project.getSelectedController() != null) {
//                            // if this device uses a cloud platform and the controller has been selected, we accept this device
//                            // if and only if the selected controller supports the cloud platform that this device uses
//                            if (project.getSelectedController().getSupportedCloudPlatform().contains(d.getCloudPlatform())) {
//                                selectableDevice.get(device).add(d);
//                            }
//                        } else {
//                            selectableDevice.get(device).add(d);
//                        }
//                    }
//                }
//            }
//        }
//
//        return selectableDevice;
//    }
//
//    public static Map<ProjectDevice, List<ProjectDevice>> getShareableDeviceList(Project project) {
//        Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> tempMap = getConstraintMap(project);
//
//        // build a map with list of generic device type that each project device has been used for example if humidity1 and altimeter1
//        // are selected to share device with temperature1. map will contain mapping for from temperature1 (project device) to list of
//        // humidity (generic device) and altimeter (generic device)
//        Map<ProjectDevice, List<GenericDevice>> mergedDeviceMap = new HashMap<>();
//        for (ProjectDevice device : project.getUnmodifiableProjectDevice()) {
//            mergedDeviceMap.put(device, new ArrayList<>());
//        }
//        for (ProjectDevice device : project.getUnmodifiableProjectDevice()) {
//            if (device.isMergeToOtherDevice()) {
//                mergedDeviceMap.get(device.getParentDevice()).add(device.getGenericDevice());
//            }
//        }
//
//        Map<ProjectDevice, List<ProjectDevice>> result = new HashMap<>();
//        for (ProjectDevice device : project.getUnmodifiableProjectDevice()) {
//            result.put(device, new ArrayList<>());
//        }
//        if (project.getSelectedController() != null) {
//            for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
//                for (ProjectDevice parentDevice : tempMap.keySet()) {
//                    if (projectDevice == parentDevice   // prevent sharing with ourselves
//                            || !parentDevice.isActualDeviceSelected()   // skip if the actual device hasn't been selected or if this device shares an actual device with other device
//                            || projectDevice.getGenericDevice() == parentDevice.getGenericDevice()  // skip if the device has identical generic type e.g. temperature1 can't be merged with temperature2
//                            || mergedDeviceMap.get(parentDevice).contains(projectDevice.getGenericDevice())) {    // skip if this device has been selected by other device with the same generic type
//                        continue;
//                    }
//                    ActualDevice d = parentDevice.getCompatibleDeviceComboItem();
//                    if (d.isSupport(project.getSelectedController(), projectDevice.getGenericDevice(), tempMap.get(projectDevice))) {
//                        if (d.getCloudConsume() != null) {
//                            // if this device uses a cloud platform and the controller has been selected, we accept this device
//                            // if and only if the selected controller supports the cloud platform that this device uses
//                            if (project.getSelectedController().getSupportedCloudPlatform().contains(d.getCloudPlatform())) {
//                                result.get(projectDevice).add(parentDevice);
//                            }
//                        } else {
//                            result.get(projectDevice).add(parentDevice);
//                        }
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    public static Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> getDeviceCompatiblePort(Project project) {
//        Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> result = new HashMap<>();
//
//        if (project.getSelectedController() == null) {
//            for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//                result.put(projectDevice, new HashMap<>());
//                if (projectDevice.isActualDeviceSelected()) {
//                    for (Peripheral peripheral : projectDevice.getCompatibleDeviceComboItem().getConnectivity())
//                        result.get(projectDevice).put(peripheral, Collections.emptyList());
//                }
//            }
//            return result;
//        }
//
//        // get every port of the controller
//        Set<DevicePort> processorPort = new HashSet<>(project.getSelectedController().getPort());
//
//        // get list of ports that have been used
//        Set<DevicePort> usedPort = new HashSet<>();
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
//                if (p.getConnectionType() != ConnectionType.I2C) {
//                    usedPort.addAll(projectDevice.getDeviceConnection().get(p));
//                }
//            }
//        }
//
//        // get list of port that conflict to the used port i.e. the used port can't be used if this port is being used
//        Map<ProjectDevice, Set<DevicePort>> conflictIfUsedPortMap = new HashMap<>();
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
//                if (p.getConnectionType() != ConnectionType.I2C) {
//                    List<DevicePort> ports = projectDevice.getDeviceConnection().get(p);
//                    Set<DevicePort> conflictIfUsedPort = processorPort.stream()
//                            .filter(devicePort -> ports.stream().anyMatch(devicePort::isConflictedTo))
//                            .collect(Collectors.toSet());
//                    conflictIfUsedPortMap.put(projectDevice, conflictIfUsedPort);
//                }
//            }
//        }
//
//        // get list of port that the used port conflict to i.e. port that can't be used if this used port is being used
//        Map<ProjectDevice, Set<DevicePort>> conflictPortMap = new HashMap<>();
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            for (Peripheral devicePeripheral : projectDevice.getDeviceConnection().keySet()) {
//                List<DevicePort> controllerPort = projectDevice.getDeviceConnection().get(devicePeripheral);
//                for (DevicePort port : controllerPort) {
//                    List<Peripheral> conflictPeripheral = port.getConflictPeripheral(devicePeripheral);
//                    if (!conflictPeripheral.isEmpty()) {
//                        conflictPortMap.put(projectDevice, new HashSet<>());
//                    }
//                    for (Peripheral p : conflictPeripheral) {
//                        processorPort.stream().filter(devicePort -> devicePort.hasPeripheral(p)).findFirst()
//                                .ifPresent(devicePort -> conflictPortMap.get(projectDevice).add(devicePort));
//                    }
//                }
//            }
//        }
//
//        // get list of split port that each device used along with the sibling ports of that port
//        Map<ProjectDevice, Set<DevicePort>> splitPortMap = new HashMap<>();
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            for (List<DevicePort> controllerPort : projectDevice.getDeviceConnection().values()) {
//                for (DevicePort port : controllerPort) {
//                    if (port.getParent() != null) {
//                        Set<DevicePort> siblingPort = project.getSelectedController().getPort().stream()
//                                .filter(devicePort -> devicePort.getParent() == port.getParent()).collect(Collectors.toSet());
//                        if (splitPortMap.containsKey(projectDevice)) {
//                            splitPortMap.get(projectDevice).addAll(siblingPort);
//                        } else {
//                            splitPortMap.put(projectDevice, siblingPort);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            Map<Peripheral, List<List<DevicePort>>> possibleDevice = new HashMap<>();
//            result.put(projectDevice, possibleDevice);
//
//            // skip if device hasn't been selected
//            if (!projectDevice.isActualDeviceSelected()) {
//                continue;
//            }
//
//            // initialize result map
//            for (Peripheral pDevice : projectDevice.getCompatibleDeviceComboItem().getConnectivity()) {
//                possibleDevice.put(pDevice, new ArrayList<>());
//            }
//
//            // bring current selection back to the possible list
//            for (Peripheral peripheral : new HashSet<>(projectDevice.getDeviceConnection().keySet())) {
//                if (possibleDevice.containsKey(peripheral)) {
//                    possibleDevice.get(peripheral).add(projectDevice.getDeviceConnection().get(peripheral));
//                } else {
//                    projectDevice.removeDeviceConnection(peripheral);
//                }
//            }
//
//            for (Peripheral pDevice : projectDevice.getCompatibleDeviceComboItem().getConnectivity()) {
//                if (pDevice.getConnectionType() == ConnectionType.NONE) {    // TODO: should be removed
//                    possibleDevice.get(pDevice).add(Collections.emptyList());
//                } else if (pDevice.getConnectionType() == ConnectionType.I2C) {    // I2C can be shared so we handle it separately
//                    for (List<DevicePort> port : project.getSelectedController().getI2CPort()) {
//                        possibleDevice.get(pDevice).add(port);
//                    }
//                } else {    // normal port that can be used once including GPIO/PWM/ANALOG, MP_GPIO/PWM/ANALOG/I2C, INEX_GPIO/PWM/ANALOG/WS2812, ...
//                    Set<DevicePort> possiblePort = new HashSet<>(processorPort);
//                    possiblePort.removeAll(usedPort);
//                    // remove port that the used port conflict to i.e. port that can't be used if this used port is being used
//                    for (ProjectDevice pd : conflictPortMap.keySet()) {
//                        if (pd != projectDevice) {
//                            possiblePort.removeAll(conflictPortMap.get(pd));
//                        }
//                    }
//                    // remove port that conflict to the used port i.e. the used port can't be used if this port is being used
//                    for (ProjectDevice pd : conflictIfUsedPortMap.keySet()) {
//                        if (pd != projectDevice) {
//                            possiblePort.removeAll(conflictIfUsedPortMap.get(pd));
//                        }
//                    }
//                    // remove split port that it's sibling has been used by other device as we may not have enough
//                    // power/ground connection for every device without using the breadboard
//                    for (ProjectDevice pd : splitPortMap.keySet()) {
//                        if (pd != projectDevice) {
//                            possiblePort.removeAll(splitPortMap.get(pd));
//                        }
//                    }
//
//                    boolean shield = projectDevice.getCompatibleDeviceComboItem().getFormFactor() == FormFactor.SHIELD;
//                    boolean integratedDevice = projectDevice.getCompatibleDeviceComboItem() instanceof IntegratedActualDevice;
//                    if (integratedDevice || shield) {   // in case of an integrated device or shield, possible port is only the port with the same peripheral
//                        List<DevicePort> mappedIntegratedPort = possiblePort.stream()
//                                .filter(devicePort1 -> devicePort1.hasPeripheral(pDevice))
//                                .collect(Collectors.toList());
//                        if (pDevice.getConnectionType() == ConnectionType.INEX_I2C) {           // order must be preserved (scl before sda)
//                            Optional<DevicePort> sclPort = mappedIntegratedPort.stream().filter(DevicePort::isSCL).findAny();
//                            Optional<DevicePort> sdaPort = mappedIntegratedPort.stream().filter(DevicePort::isSDA).findAny();
//                            if (sclPort.isPresent() && sdaPort.isPresent()) {
//                                possibleDevice.get(pDevice).add(List.of(sclPort.get(), sdaPort.get()));
//                            }
//                        } else if (pDevice.getConnectionType() == ConnectionType.INEX_UART) {   // order must be preserved
//                            throw new UnsupportedOperationException();
//                        } else if (mappedIntegratedPort.size() > 1) {
//                            // normal peripheral should use only 1 port except I2C, INEX_I2C and INEX_UART that we have handled
//                            // so if we reach here, someone may have added new type without handle it properly
//                            throw new IllegalStateException();
//                        } else if (mappedIntegratedPort.size() == 1) {
//                            possibleDevice.get(pDevice).add(mappedIntegratedPort);
//                        }
//                        // else port may have been used by other devices
//                    } else {
//                        if (pDevice.getConnectionType() == ConnectionType.INEX_I2C) {           // order must be preserved (scl before sda)
//                            Optional<DevicePort> sclPort = possiblePort.stream().filter(DevicePort::isSCL).findAny();
//                            Optional<DevicePort> sdaPort = possiblePort.stream().filter(DevicePort::isSDA).findAny();
//                            if (sclPort.isPresent() && sdaPort.isPresent()) {
//                                possibleDevice.get(pDevice).add(List.of(sclPort.get(), sdaPort.get()));
//                            }
//                        } else if (pDevice.getConnectionType() == ConnectionType.INEX_UART) {   // order must be preserved
//                            throw new UnsupportedOperationException();
//                        } else if (pDevice.getConnectionType() == ConnectionType.UART) {
//                            Optional<DevicePort> rxPort = possiblePort.stream().filter(DevicePort::isRX).findAny();
//                            Optional<DevicePort> txPort = possiblePort.stream().filter(DevicePort::isTX).findAny();
//                            if (rxPort.isPresent() && txPort.isPresent()) {
//                                possibleDevice.get(pDevice).add(List.of(rxPort.get(), txPort.get()));
//                            }
//                        } else {
//                            // for each port in the possible port list, add to the result if it supported
//                            for (DevicePort pPort : possiblePort) {
//                                if (pPort.getType() != DevicePortType.INTERNAL && pPort.isSupport(pDevice)) {
//                                    possibleDevice.get(pDevice).add(Collections.singletonList(pPort));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//    public static List<ActualDevice> getSupportedController(Project project) {
//        return DeviceLibrary.INSTANCE.getCompatibleDeviceComboItem().stream()
//                .filter(device -> (device.getDeviceType() == DeviceType.CONTROLLER)
//                        && device.getSupportedPlatform().contains(project.getSelectedPlatform()))
//                // TODO: getCloudPlatformUsed() is based on an actual device selected which doesn't work in this case as the controller hasn't been selected yet
//                // && device.getSupportedCloudPlatform().containsAll(project.getCloudPlatformUsed()))
//                .collect(Collectors.toUnmodifiableList());
//    }
//
    public static ProjectMappingResult validateDeviceAssignment(Project project) {
        ProjectConfiguration configuration = project.getProjectConfiguration();
        if (configuration.getController() == null) {
            return ProjectMappingResult.NO_MCU_SELECTED;
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (!configuration.isActualDeviceSelected(projectDevice) && !configuration.isUsedSameDevice(projectDevice)) {
                return ProjectMappingResult.NOT_SELECT_DEVICE;
            }

            // for each connectivity required, check if it has been connected and indicate error if it hasn't
            if (configuration.isActualDeviceSelected(projectDevice)) {
                ProjectDevice root = configuration.getParentDevice(projectDevice).orElse(projectDevice);
                if (project.getProjectConfiguration().getUnmodifiableDevicePinPortConnections().stream().noneMatch(connection -> connection.getTo() == root)) {
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
//            projectDevice.removeAllDeviceConnection();
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
//            } else if (projectDevice.isActualDeviceSelected()) {
//                // try assign port based on the device that user has selected
//                done = assignPort(project, projectDevice);
//            } else {
//                // try each support device until we find the one that works
//                for (ActualDevice actualDevice : supportedDeviceMap.get(projectDevice)) {
//                    projectDevice.removeAllDeviceConnection();
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