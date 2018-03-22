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
    private final ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        reInitialize();
    }

    public void reInitialize() {
        Map<ProjectDevice, List<Device>> deviceList = DeviceMapper.getSupportedDeviceList(project);
        compatibleDeviceList.set(deviceList);

        Map<ProjectDevice, List<DevicePort>> portList = new HashMap<>();

        Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> tmp = DeviceMapper.getDeviceCompatiblePort(project);
//        for (ProjectDevice projectDevice : tmp.keySet()) {
//            Map<Peripheral, List<DevicePort>> possibleConnection = tmp.get(projectDevice);
//            if (projectDevice.getActualDevice() != null) {
//                Peripheral firstPeripheral = projectDevice.getActualDevice().getConnectivity().get(0);
//                portList.put(projectDevice, possibleConnection.get(firstPeripheral));
//            } else {
//                portList.put(projectDevice, new ArrayList<>());
//            }
//        }
        compatiblePortList.set(tmp);
    }


    public List<Device> getCompatibleDevice(ProjectDevice projectDevice) {
        return compatibleDeviceList.get().get(projectDevice);
    }

    public Map<Peripheral, List<List<DevicePort>>> getCompatiblePort(ProjectDevice projectDevice) {
        return compatiblePortList.get().get(projectDevice);
    }

    public ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceListProperty() {
        return compatibleDeviceList;
    }

    public ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortListProperty() {
        return compatiblePortList;
    }

    public void setDevice(ProjectDevice projectDevice, Device device) {
        projectDevice.setActualDevice(device);
    }

    public void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(peripheral, port);
        reInitialize();
    }

    public void removePeripheral(ProjectDevice projectDevice) {
        // TODO: assume a device only has 1 peripheral
        //projectDevice.removeDeviceConnection(projectDevice.getActualDevice().getConnectivity().get(0));
        for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
            projectDevice.removeDeviceConnection(p);
        }
    }

    public List<ProjectDevice> getAllDevice() {
        return project.getAllDevice();
    }
}
