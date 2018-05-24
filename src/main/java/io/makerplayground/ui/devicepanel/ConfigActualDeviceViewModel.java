package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.Device;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.DevicePort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.DeviceType;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;
    private DeviceMapper.DeviceMapperResult deviceMapperResult;
    private Callback callback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        reInitialize();
    }

    private void reInitialize() {
        deviceMapperResult = DeviceMapper.autoAssignDevices(project);
        if (deviceMapperResult == DeviceMapper.DeviceMapperResult.OK) {
            Map<ProjectDevice, List<Device>> deviceList = DeviceMapper.getSupportedDeviceList(project);
            compatibleDeviceList.set(deviceList);

            Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>> tmp = DeviceMapper.getDeviceCompatiblePort(project);
            compatiblePortList.set(tmp);
        } else {
            compatibleDeviceList.set(null);
            compatiblePortList.set(null);
        }
        if (callback != null) {
            callback.call();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public DeviceMapper.DeviceMapperResult getDeviceMapperResult() {
        return deviceMapperResult;
    }

    public List<Device> getCompatibleDevice(ProjectDevice projectDevice) {
        return compatibleDeviceList.get().get(projectDevice);
    }

    public Map<Peripheral, List<List<DevicePort>>> getCompatiblePort(ProjectDevice projectDevice) {
        return compatiblePortList.get().get(projectDevice);
    }

    public List<Device> getCompatibleControllerDevice() {
        return DeviceMapper.getSupportedController(project);
    }

    public void setController(Device device) {
        project.setController(device);
        reInitialize();
    }

    public Device getSelectedController() {
        return project.getController();
    }

    public ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceListProperty() {
        return compatibleDeviceList;
    }

    public ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortListProperty() {
        return compatiblePortList;
    }

    public void setDevice(ProjectDevice projectDevice, Device device) {
        projectDevice.setActualDevice(device);
        reInitialize();
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
        reInitialize();
    }

    public List<ProjectDevice> getAllDevice() {
        return project.getAllDevice();
    }
}
