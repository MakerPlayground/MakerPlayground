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

package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.device.actual.ConnectionType;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.device.actual.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.Scene;
import io.makerplayground.project.UserSetting;
import io.makerplayground.project.expression.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class DeviceMapper {
    public static Map<ProjectDevice, List<ActualDevice>> getSupportedDeviceList(Project project) {
        Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> tempMap = new HashMap<>();

        for (ProjectDevice projectDevice : project.getAllDevice()) {
            tempMap.put(projectDevice, new HashMap<>());
        }

        for (Scene s : project.getScene()) {
            for (UserSetting u : s.getSetting()) {
                ProjectDevice projectDevice = u.getDevice();
                Map<Action, Map<Parameter, Constraint>> compatibility = tempMap.get(projectDevice);
                for (Parameter parameter : u.getValueMap().keySet()) {
                    Action action = u.getAction();
                    Expression o = u.getValueMap().get(parameter);

                    if (!compatibility.containsKey(action)) {
                        compatibility.put(action, new HashMap<>());
                    }

                    Constraint newConstraint = null;
                    if (parameter.getDataType() == DataType.INTEGER || parameter.getDataType() == DataType.DOUBLE) {
                        if (o instanceof NumberWithUnitExpression) {
                            NumberWithUnit n = ((NumberWithUnitExpression) o).getNumberWithUnit();
                            newConstraint = Constraint.createNumericConstraint(n.getValue(), n.getValue(), n.getUnit());
                        } else if (o instanceof CustomNumberExpression) {
                            // TODO: should be calculated from the expression or use range of parameter value
                            newConstraint = Constraint.NONE;
                        } else if (o instanceof ValueLinkingExpression) {
                            ValueLinkingExpression exp = (ValueLinkingExpression) o;
                            newConstraint = Constraint.createNumericConstraint(exp.getDestinationLowValue().getValue(), exp.getDestinationHighValue().getValue(), exp.getDestinationLowValue().getUnit());
                        } else if (o instanceof ProjectValueExpression) {
                            ProjectValueExpression exp = (ProjectValueExpression) o;
                            newConstraint = ((NumericConstraint) parameter.getConstraint()).intersect(exp.getProjectValue().getValue().getConstraint(), Function.identity());
                        } else {
                            throw new IllegalStateException("Constraint is not defined for expression type: " + o.getClass().getCanonicalName());
                        }
                    } else if (parameter.getDataType() == DataType.STRING || parameter.getDataType() == DataType.ENUM) {
                        newConstraint = Constraint.createCategoricalConstraint(((SimpleStringExpression) o).getString());
                    } else {
                        continue;
                    }

                    Map<Parameter, Constraint> parameterMap = compatibility.get(action);
                    if (parameterMap.containsKey(parameter)) {
                        Constraint oldConstraint = parameterMap.get(parameter);
                        parameterMap.replace(parameter, oldConstraint.union(newConstraint));
                    } else {
                        parameterMap.put(parameter, newConstraint);
                    }
                }
            }
        }

        // Print to see result
//        for (ProjectDevice device : tempMap.keySet()) {
//            System.out.println(device.getName());
//            for (Action action : tempMap.get(device).keySet()) {
//                System.out.println(action.getName());
//                for (Parameter parameter : tempMap.get(device).get(action).keySet()) {
//                    System.out.println(parameter.getName() + tempMap.get(device).get(action).get(parameter));
//                }
//            }
//        }

        List<ActualDevice> actualDevice = new ArrayList<>(DeviceLibrary.INSTANCE.getActualDevice(project.getPlatform()));
        // append with integrated device of the current controller if existed
        if (project.getController() != null) {
            actualDevice.addAll(project.getController().getIntegratedDevices());
        }

        // Get the list of compatible device
        Map<ProjectDevice, List<ActualDevice>> selectableDevice = new HashMap<>();
        for (ProjectDevice device : tempMap.keySet()) {
            selectableDevice.put(device, new ArrayList<>());
            for (ActualDevice d : actualDevice) {
                if (d.isSupport(device.getGenericDevice(), tempMap.get(device))) {  // TODO: edit to filter platform
                    selectableDevice.get(device).add(d);
                }
            }
        }

        return selectableDevice;
    }

    public static Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> getDeviceCompatiblePort(Project project) {
        Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> result = new HashMap<>();

        if (project.getController() == null) {
            for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
                result.put(projectDevice, new HashMap<>());
                for (Peripheral peripheral : projectDevice.getActualDevice().getConnectivity())
                    result.get(projectDevice).put(peripheral, Collections.emptyList());
            }
            return result;
        }

        // get every port of the controller
        Set<DevicePort> processorPort = new HashSet<>(project.getController().getPort());

        // get list of ports that have been used
        Set<DevicePort> usedPort = new HashSet<>();
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                if (p.getConnectionType() != ConnectionType.I2C) {
                    usedPort.addAll(projectDevice.getDeviceConnection().get(p));
                }
            }
        }

        // get list of port that conflict to the used port i.e. the used port can't be used if this port is being used
        Map<ProjectDevice, Set<DevicePort>> conflictIfUsedPortMap = new HashMap<>();
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                if (p.getConnectionType() != ConnectionType.I2C) {
                    List<DevicePort> ports = projectDevice.getDeviceConnection().get(p);
                    Set<DevicePort> conflictIfUsedPort = processorPort.stream()
                            .filter(devicePort -> ports.stream().anyMatch(devicePort::isConflictedTo))
                            .collect(Collectors.toSet());
                    conflictIfUsedPortMap.put(projectDevice, conflictIfUsedPort);
                }
            }
        }

        // get list of port that the used port conflict to i.e. port that can't be used if this used port is being used
        Map<ProjectDevice, Set<DevicePort>> conflictPortMap = new HashMap<>();
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            for (Peripheral devicePeripheral : projectDevice.getDeviceConnection().keySet()) {
                List<DevicePort> controllerPort = projectDevice.getDeviceConnection().get(devicePeripheral);
                for (DevicePort port : controllerPort) {
                    List<Peripheral> conflictPeripheral = port.getConflictPeripheral(devicePeripheral);
                    if (!conflictPeripheral.isEmpty()) {
                        conflictPortMap.put(projectDevice, new HashSet<>());
                    }
                    for (Peripheral p : conflictPeripheral) {
                        processorPort.stream().filter(devicePort -> devicePort.hasPeripheral(p)).findFirst()
                                .ifPresent(devicePort -> conflictPortMap.get(projectDevice).add(devicePort));
                    }
                }
            }
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            Map<Peripheral, List<List<DevicePort>>> possibleDevice = new HashMap<>();
            for (Peripheral pDevice : projectDevice.getActualDevice().getConnectivity()) {
                possibleDevice.put(pDevice, new ArrayList<>());
            }

            // bring current selection back to the possible list
            for (Map.Entry<Peripheral, List<DevicePort>> connectivity : projectDevice.getDeviceConnection().entrySet()) {
                possibleDevice.get(connectivity.getKey()).add(connectivity.getValue());
            }

            for (Peripheral pDevice : projectDevice.getActualDevice().getConnectivity()) {
                if (projectDevice.getActualDevice() instanceof IntegratedActualDevice) {
                    // in case of an integrated device, possible port is only the port with the same name
                    processorPort.stream().filter(devicePort -> devicePort.getName().equals(projectDevice.getActualDevice().getPort(pDevice).get(0).getName()))
                            .findFirst().ifPresent(devicePort -> possibleDevice.get(pDevice).add(Collections.singletonList(devicePort)));
                } else if (pDevice.getConnectionType() == ConnectionType.NONE) {    // TODO: should be removed
                    possibleDevice.get(pDevice).add(Collections.emptyList());
                } else if (pDevice.getConnectionType() == ConnectionType.I2C) {
                    DevicePort sclPort = processorPort.stream().filter(DevicePort::isSCL).findAny().get();
                    DevicePort sdaPort = processorPort.stream().filter(DevicePort::isSDA).findAny().get();
                    possibleDevice.get(pDevice).add(Arrays.asList(sclPort, sdaPort));
                } else {
                    Set<DevicePort> possiblePort = new HashSet<>(processorPort);
                    possiblePort.removeAll(usedPort);
                    // remove port that the used port conflict to i.e. port that can't be used if this used port is being used
                    for (ProjectDevice pd : conflictPortMap.keySet()) {
                        if (pd != projectDevice) {
                            possiblePort.removeAll(conflictPortMap.get(pd));
                        }
                    }
                    // remove port that conflict to the used port i.e. the used port can't be used if this port is being used
                    for (ProjectDevice pd : conflictIfUsedPortMap.keySet()) {
                        if (pd != projectDevice) {
                            possiblePort.removeAll(conflictIfUsedPortMap.get(pd));
                        }
                    }
                    // for each port in the possible port list, add to the result if it supported
                    for (DevicePort pPort : possiblePort) {
                        if (pPort.isSupport(pDevice)) {
                            possibleDevice.get(pDevice).add(Collections.singletonList(pPort));
                        }
                    }
                }
            }

            result.put(projectDevice, possibleDevice);
        }

        return result;
    }

    public static List<ActualDevice> getSupportedController(Project project) {
        return DeviceLibrary.INSTANCE.getActualDevice().stream()
                .filter(device -> (device.getDeviceType() == DeviceType.CONTROLLER)
                        && (device.getSupportedPlatform().contains(project.getPlatform()))
                        && (device.getSupportedCloudPlatform().containsAll(project.getCloudPlatformUsed())))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static DeviceMapperResult autoAssignDevices(Project project) {
        // Auto select a controller if it hasn't been selected. If there aren't any supported MCU for the current platform, return error
        if (project.getController() == null) {
            List<ActualDevice> supportController = getSupportedController(project);
            if (supportController.isEmpty()) {
                return DeviceMapperResult.NO_MCU_SELECTED;
            } else {
                project.setController(supportController.get(0));
            }
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            // Assign this device if only user check auto
            if (projectDevice.isAutoSelectDevice()) {
                // Set actual device by selecting first element
                Map<ProjectDevice, List<ActualDevice>> deviceList = getSupportedDeviceList(project);
                if (deviceList.get(projectDevice).isEmpty()) {
                    return DeviceMapperResult.NO_SUPPORT_DEVICE;
                }
                projectDevice.setActualDevice(deviceList.get(projectDevice).get(0));
            }
        }

        // reclaim ports from unused devices
        for (ProjectDevice projectDevice : project.getAllDeviceUnused()) {
            projectDevice.setAutoSelectDevice(true);
            projectDevice.removeAllDeviceConnection();
        }

        Map<ProjectDevice, List<ActualDevice>> supportedDeviceMap = getSupportedDeviceList(project);
        Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> portList;
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            // assign this device only if user selects auto
            if (projectDevice.isAutoSelectDevice()) {
                //
                if (supportedDeviceMap.get(projectDevice).isEmpty()) {
                    return DeviceMapperResult.NO_SUPPORT_DEVICE;
                }

                // try each support device until we find the one that works
                boolean error = false;
                for (ActualDevice actualDevice : supportedDeviceMap.get(projectDevice)) {
                    projectDevice.removeAllDeviceConnection();
                    projectDevice.setActualDevice(actualDevice);

                    // set port to the first compatible port
                    error = false;
                    for (Peripheral devicePeripheral : projectDevice.getActualDevice().getConnectivity()) {
                        // portList needs to be recalculated since some ports have been used in the previous loop.
                        portList = getDeviceCompatiblePort(project);
                        if (!projectDevice.getDeviceConnection().containsKey(devicePeripheral)) {
                            List<List<DevicePort>> port = portList.get(projectDevice).get(devicePeripheral);
                            if (port.isEmpty()) {
                                error = true;
                                break;
                            }
                            projectDevice.setDeviceConnection(devicePeripheral, port.get(0));
                        }
                    }

                    // done if error is false
                    if (!error) {
                        break;
                    }
                }
                if (error) {
                    return DeviceMapperResult.NOT_ENOUGH_PORT;
                }
            }
        }
        return DeviceMapperResult.OK;
    }
}