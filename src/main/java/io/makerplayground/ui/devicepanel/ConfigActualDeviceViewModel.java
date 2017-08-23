package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
//    private final ObservableMap<ProjectDevice, ObservableList<Device>> compatibleDeviceList;
//    private final ObservableMap<ProjectDevice, ObservableList<Peripheral>> compatiblePortList;
    private final ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, List<DevicePort>>> compatiblePortList;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        reInitialize();
    }

    public void reInitialize() {
        Map<ProjectDevice, List<Device>> deviceList = DeviceMapper.getSupportedDeviceList(project);
        compatibleDeviceList.set(deviceList);
//        for (ProjectDevice projectDevice : deviceList.keySet()) {
//            if (compatibleDeviceList.containsKey(projectDevice)) {
//                compatibleDeviceList.get(projectDevice).setAll(deviceList.get(projectDevice));
//            } else {
//                compatibleDeviceList.put(projectDevice, FXCollections.observableArrayList(deviceList.get(projectDevice)));
//            }
//        }


        Map<ProjectDevice, List<DevicePort>> portList = new HashMap<>();
//        for (Map.Entry<ProjectDevice, Map<Peripheral, List<Peripheral>>> entry : DeviceMapper.getDeviceCompatiblePort(project).entrySet()) {
//            portList.put(entry.getKey(), entry.getValue().get(entry.getKey().getActualDevice().getConnectivity().get(0)));
//        }
        Map<ProjectDevice, Map<Peripheral, List<DevicePort>>> tmp = DeviceMapper.getDeviceCompatiblePort(project);
        for (ProjectDevice projectDevice : tmp.keySet()) {
            Map<Peripheral, List<DevicePort>> possibleConnection = tmp.get(projectDevice);
            if (projectDevice.getActualDevice() != null) {
                Peripheral firstPeripheral = projectDevice.getActualDevice().getConnectivity().get(0);
                portList.put(projectDevice, possibleConnection.get(firstPeripheral));
            } else {
                portList.put(projectDevice, new ArrayList<>());
            }
        }
        compatiblePortList.set(portList);
//        for (ProjectDevice projectDevice : portList.keySet()) {
//            if (compatiblePortList.containsKey(projectDevice)) {
//                compatiblePortList.get(projectDevice).setAll(portList.get(projectDevice));
//            } else {
//                compatiblePortList.put(projectDevice, FXCollections.observableArrayList(portList.get(projectDevice)));
//            }
//        }
    }

//    public ObservableList<ProjectDevice> getOutputDevice() {
//        return project.getOutputDevice();
//    }
//
//    public ObservableList<ProjectDevice> getInputDevice() {
//        return project.getInputDevice();
//    }
//
//    public Map<ProjectDevice, List<Device>> getCompatibleDeviceList() {
//        return compatibleDeviceList;
//    }

    public List<Device> getCompatibleDevice(ProjectDevice projectDevice) {
        return compatibleDeviceList.get().get(projectDevice);
    }

    public List<DevicePort> getCompatiblePort(ProjectDevice projectDevice) {
        return compatiblePortList.get().get(projectDevice);
    }

    public ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceListProperty() {
        return compatibleDeviceList;
    }

    public ObjectProperty<Map<ProjectDevice, List<DevicePort>>> compatiblePortListProperty() {
        return compatiblePortList;
    }

    public void setDevice(ProjectDevice projectDevice, Device device) {
        projectDevice.setActualDevice(device);
        //reInitialize();
    }

    public void setPeripheral(ProjectDevice projectDevice, DevicePort port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(projectDevice.getActualDevice().getConnectivity().get(0), port);
        reInitialize();
    }

    public void removePeripheral(ProjectDevice projectDevice) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.removeDeviceConnection(projectDevice.getActualDevice().getConnectivity().get(0));
        //reInitialize();
    }

    public ObservableList<ProjectDevice> getAllDevice() {
        return project.getAllDevice();
    }

    // Must be called after selected controller
//    public Map<ProjectDevice, List<Peripheral>> getDeviceCompatiblePort() {
//        Map<ProjectDevice, List<Peripheral>> result = new HashMap<>();
//
//        if (project.getController().getController() == null)
//        {
//            for (ProjectDevice projectDevice : project.getAllDevice()) {
//                result.put(projectDevice, Collections.emptyList());
//            }
//            return result;
//        }
//
//        List<Peripheral> processorPort = project.getController().getController().getConnectivity();
//
//        for (ProjectDevice projectDevice : project.getAllDevice()) {
//            //connection from this device (key) to the processor (value)
//            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
//                if (processorPort.contains(p))
//                    processorPort.remove(p);
//            }
//        }
//
//        for (ProjectDevice projectDevice : project.getAllDevice()) {
//            List<Peripheral> possibleDevice = new ArrayList<>();
//            if (!projectDevice.isAutoSelectDevice() && (projectDevice.getActualDevice() != null)) { // calculate possible only if actual device is selected
//                for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
//                    possibleDevice.add(p);
//                }
//                for (Peripheral p : processorPort) {
//                    if (projectDevice.getActualDevice().getConnectivity().get(0).getType() == p.getType()) {
//                        possibleDevice.add(p);
//                    }
//                }
//            }
//            result.put(projectDevice, possibleDevice);
//        }
//
//        return result;
//    }
}
