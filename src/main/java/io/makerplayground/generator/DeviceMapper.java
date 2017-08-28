package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.helper.ConnectionType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.Scene;
import io.makerplayground.project.UserSetting;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class DeviceMapper {
    public static Map<ProjectDevice, List<Device>> getSupportedDeviceList(Project project) {
        List<Device> actualDevice = DeviceLibrary.INSTANCE.getActualDevice();
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
                    Object o = u.getValueMap().get(parameter);

                    if (!compatibility.containsKey(action)) {
                        compatibility.put(action, new HashMap<>());
                    }

                    Constraint newConstraint = null;
                    if (parameter.getDataType() == DataType.INTEGER || parameter.getDataType() == DataType.DOUBLE) {
                        NumberWithUnit n = (NumberWithUnit) o;
                        newConstraint = Constraint.createNumericConstraint(n.getValue(), n.getValue(), n.getUnit());
                    } else if (parameter.getDataType() == DataType.STRING || parameter.getDataType() == DataType.ENUM) {
                        newConstraint = Constraint.createCategoricalConstraint((String) o);
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

        if (project.getController().getController() == null)
        {
            for (ProjectDevice projectDevice : project.getAllDevice()) {
                result.put(projectDevice, new HashMap<>());
                for (Peripheral peripheral : projectDevice.getActualDevice().getConnectivity())
                    result.get(projectDevice).put(peripheral, Collections.emptyList());
            }
            return result;
        }

        // Get every port of the controller
        List<DevicePort> processorPort = new ArrayList<>(project.getController().getController().getPort());

        // remove port that has been used (manually by user)
        for (ProjectDevice projectDevice : project.getAllDevice()) {
            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                if (p.getConnectionType() != ConnectionType.I2C)
                    processorPort.removeAll(projectDevice.getDeviceConnection().get(p));
            }
        }

        for (ProjectDevice projectDevice : project.getAllDevice()) {
            Map<Peripheral, List<List<DevicePort>>> possibleDevice = new HashMap<>();
            if (projectDevice.getActualDevice() != null) { // calculate possible only if actual device is selected
                for (Peripheral pDevice : projectDevice.getActualDevice().getConnectivity()) {
                    possibleDevice.put(pDevice, new ArrayList<>());
                }
                // bring current selection back to the possible list
                for (Map.Entry<Peripheral, List<DevicePort>> connectivity : projectDevice.getDeviceConnection().entrySet()) {
                    possibleDevice.get(connectivity.getKey()).add(connectivity.getValue());
                }

                for (Peripheral pDevice : projectDevice.getActualDevice().getConnectivity()) {
                    if ((pDevice.getConnectionType() == ConnectionType.GPIO) || (pDevice.getConnectionType() == ConnectionType.ANALOG)
                            || (pDevice.getConnectionType() == ConnectionType.PWM)) {
                        for (DevicePort pPort : processorPort) {
                            if (pPort.isSupport(pDevice)) {
                                possibleDevice.get(pDevice).add(Collections.singletonList(pPort));
                            }
                        }
                    } else if (pDevice.getConnectionType() == ConnectionType.I2C) {
                        DevicePort sclPort = processorPort.stream().filter(DevicePort::isSCL).findAny().get();
                        DevicePort sdaPort = processorPort.stream().filter(DevicePort::isSDA).findAny().get();
                        possibleDevice.get(pDevice).add(Arrays.asList(sclPort, sdaPort));
                    } else {
                        throw new IllegalStateException("We don't support this type of device!!!");
                    }
                }
                // case auto assign device
            } else {
                System.out.println("Skip : " + projectDevice.getName());
            }

            result.put(projectDevice, possibleDevice);
        }

        return result;
    }

    public static boolean autoAssignDevices(Project project) {
        //List<Peripheral> processorPort = new ArrayList<>(project.getController().getController().getConnectivity());

        for (ProjectDevice projectDevice : project.getAllDevice()) {

            if (projectDevice.getActualDevice() == null) {
            //if (projectDevice.getActualDevice() == null) {
                // Set actual device by selecting first element
                Map<ProjectDevice, List<Device>> deviceList = getSupportedDeviceList(project);
                projectDevice.setActualDevice(deviceList.get(projectDevice).get(0));

                //Map<ProjectDevice, List<Peripheral>> portList = getDeviceCompatiblePort(project);

                for (Peripheral devicePeripheral : projectDevice.getActualDevice().getConnectivity()) {
                    Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> portList = getDeviceCompatiblePort(project);
                    if (!projectDevice.getDeviceConnection().containsKey(devicePeripheral)) {
                        List<List<DevicePort>> port = portList.get(projectDevice).get(devicePeripheral);
                        if (!port.isEmpty()) {
                            projectDevice.setDeviceConnection(devicePeripheral, port.get(0));
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
