package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.helper.*;
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
    public static Map<ProjectDevice, List<Device>> getSupportedDeviceList(Project project) {
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
        
        // Get the list of compatible device
        List<Device> actualDevice = DeviceLibrary.INSTANCE.getActualDevice(project.getPlatform());
        Map<ProjectDevice, List<Device>> selectableDevice = new HashMap<>();
        for (ProjectDevice device : tempMap.keySet()) {
            selectableDevice.put(device, new ArrayList<>());
            for (Device d : actualDevice) {
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

        // Get every port of the controller
        List<DevicePort> processorPort = new ArrayList<>(project.getController().getPort());

        // remove port that has been used (manually by user)
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                if (p.getConnectionType() != ConnectionType.I2C) {
                    processorPort.removeAll(projectDevice.getDeviceConnection().get(p));
                }
                // SPECIAL CASE 1: if both pin of D2 is used, D3 can't be used
                if (project.getController() != null && project.getController().getId().equals("MP-0000")) {
                    if ((p == Peripheral.MP_GPIO_DUAL_1) || (p == Peripheral.MP_PWM_DUAL_1)) {
                        for (DevicePort controllerPort : projectDevice.getDeviceConnection().get(p)) {
                            for (DevicePort.DevicePortFunction function : controllerPort.getFunction()) {
                                if ((function.getPeripheral() == Peripheral.MP_GPIO_DUAL_2) || (function.getPeripheral() == Peripheral.MP_PWM_DUAL_2)) {
                                    processorPort.removeAll(project.getController().getPort(Peripheral.MP_GPIO_SINGLE_3));
                                    processorPort.removeAll(project.getController().getPort(Peripheral.MP_GPIO_DUAL_3));
                                    processorPort.removeAll(project.getController().getPort(Peripheral.MP_PWM_SINGLE_3));
                                    processorPort.removeAll(project.getController().getPort(Peripheral.MP_PWM_DUAL_3));
                                }
                            }
                        }
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
                if (pDevice.getConnectionType() == ConnectionType.NONE) {
                    possibleDevice.get(pDevice).add(Collections.emptyList());
                }
                else if (pDevice.getConnectionType() == ConnectionType.I2C) {
                    DevicePort sclPort = processorPort.stream().filter(DevicePort::isSCL).findAny().get();
                    DevicePort sdaPort = processorPort.stream().filter(DevicePort::isSDA).findAny().get();
                    possibleDevice.get(pDevice).add(Arrays.asList(sclPort, sdaPort));
                } else {
                    for (DevicePort pPort : processorPort) {
                        if (pPort.isSupport(pDevice)) {
                            possibleDevice.get(pDevice).add(Collections.singletonList(pPort));
                        }
                    }
                }
            }

            // SPECIAL CASE 2: try not to connect device using 2 pins to D2 as it will block D3
            if (project.getController() != null && project.getController().getId().equals("MP-0000")) {
                for (Peripheral peripheral : possibleDevice.keySet()) {
                    if ((peripheral == Peripheral.MP_GPIO_DUAL_1) || (peripheral == Peripheral.MP_PWM_DUAL_1)) {
                        List<List<DevicePort>> port = possibleDevice.get(peripheral);
                        for (int i = port.size() - 1; i >= 0; i--) {
                            boolean needSwap = false;
                            for (DevicePort.DevicePortFunction function : port.get(i).get(0).getFunction()) {
                                if ((function.getPeripheral() == Peripheral.MP_GPIO_DUAL_2) || (function.getPeripheral() == Peripheral.MP_PWM_DUAL_2)) {
                                    needSwap = true;
                                }
                            }
                            if (needSwap) {
                                port.add(port.remove(i));
                            }
                        }
                    }
                }
            }

            result.put(projectDevice, possibleDevice);
        }

        return result;
    }

    public static List<Device> getSupportedController(Project project) {
        return DeviceLibrary.INSTANCE.getActualDevice().stream()
                .filter(device -> (device.getDeviceType() == DeviceType.CONTROLLER)
                        && (device.getSupportedPlatform().contains(project.getPlatform())))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public enum DeviceMapperResult {
        OK, NOT_ENOUGH_PORT, NO_SUPPORT_DEVICE, NO_MCU_SELECTED
    }

    public static DeviceMapperResult autoAssignDevices(Project project) {
        // Auto select a controller if it hasn't been selected. If there aren't any supported MCU for the current platform, return error
        if (project.getController() == null) {
            List<Device> supportController = getSupportedController(project);
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
                Map<ProjectDevice, List<Device>> deviceList = getSupportedDeviceList(project);
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

        List<ProjectDevice> deviceList = new ArrayList<>(project.getAllDeviceUsed());

        // SPECIAL CASE 3: connect MP_*_DUAL first
        if (project.getController() != null && project.getController().getId().equals("MP-0000")) {
            deviceList.sort((o1, o2) -> {
                boolean b1 = o1.getActualDevice().getConnectivity().contains(Peripheral.MP_GPIO_DUAL_1)
                        || o1.getActualDevice().getConnectivity().contains(Peripheral.MP_PWM_DUAL_1);
                boolean b2 = o2.getActualDevice().getConnectivity().contains(Peripheral.MP_GPIO_DUAL_1)
                        || o2.getActualDevice().getConnectivity().contains(Peripheral.MP_PWM_DUAL_1);
                if (b1 == b2) {
                    return 0;
                } else if (b1) {
                    return -1;
                } else {
                    return 1;
                }
            });
        }

        Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> portList;
        for (ProjectDevice projectDevice : deviceList) {
            // Set port to the first compatible port
            for (Peripheral devicePeripheral : projectDevice.getActualDevice().getConnectivity()) {
                // port list is needed to be recalculated since port is allocated in the previous loop.
                portList = getDeviceCompatiblePort(project);
                if (!projectDevice.getDeviceConnection().containsKey(devicePeripheral)) {
                    List<List<DevicePort>> port = portList.get(projectDevice).get(devicePeripheral);
                    if (port.isEmpty()) {
                        return DeviceMapperResult.NOT_ENOUGH_PORT;
                    }
                    projectDevice.setDeviceConnection(devicePeripheral, port.get(0));
                }
            }
        }
        return DeviceMapperResult.OK;
    }
}
