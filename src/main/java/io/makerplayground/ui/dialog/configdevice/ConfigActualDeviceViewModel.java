package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.devicepane.devicepanel.Callback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, List<Device>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;
    private DeviceMapper.DeviceMapperResult deviceMapperResult;
    private Callback platformChangedCallback;
    private Callback controllerChangedCallback;
    private Callback deviceConfigChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        validateDevice();
    }

    private void validateDevice() {
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
    }

    public void setPlatformChangedCallback(Callback platformChangedCallback) {
        this.platformChangedCallback = platformChangedCallback;
    }

    public void setControllerChangedCallback(Callback controllerChangedCallback) {
        this.controllerChangedCallback = controllerChangedCallback;
    }

    public void setDeviceConfigChangedCallback(Callback deviceConfigChangedCallback) {
        this.deviceConfigChangedCallback = deviceConfigChangedCallback;
    }

    public void removeDeviceConfigChangedCallback() {
        this.deviceConfigChangedCallback = null;
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

    public void setPlatform(Platform platform) {
        project.setPlatform(platform);
        validateDevice();
        if (platformChangedCallback != null) {
            platformChangedCallback.call();
        }
    }

    public Platform getSelectedPlatform() {
        return project.getPlatform();
    }

    public void setController(Device device) {
        project.setController(device);
        validateDevice();
        if (controllerChangedCallback != null) {
            controllerChangedCallback.call();
        }
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
        validateDevice();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
        }
    }

    public void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(peripheral, port);
        validateDevice();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
        }
    }

    public void removePeripheral(ProjectDevice projectDevice) {
        // TODO: assume a device only has 1 peripheral
        //projectDevice.removeDeviceConnection(projectDevice.getActualDevice().getConnectivity().get(0));
        for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
            projectDevice.removeDeviceConnection(p);
        }
        validateDevice();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
        }
    }

    public List<ProjectDevice> getAllDevice() {
        return project.getAllDevice();
    }

}
