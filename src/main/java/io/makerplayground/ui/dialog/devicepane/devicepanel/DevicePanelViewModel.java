package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.deprecated.DevicePanelIconViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelViewModel {
    private final Project project;
    private final ObjectProperty<Platform> platformProperty;


    public DevicePanelViewModel(Project project) {
        this.project = project;
        this.platformProperty = new SimpleObjectProperty<>(project.getPlatform());
        // update ourselves when project's platform has been changed else where
        this.project.platformProperty().addListener((observable, oldValue, newValue) -> platformProperty.set(newValue));
        // write back to project when view changed
        this.platformProperty.addListener((observable, oldValue, newValue) -> project.setPlatform(newValue));
    }

    public Project getProject() {
        return project;
    }

    public ObservableList<ProjectDevice> getSensor() {
        return project.getSensor();
    }
    public ObservableList<ProjectDevice> getActuator() {
        return project.getActuator();
    }
    public ObservableList<ProjectDevice> getConnectivity() {
        return project.getConnectivity();
    }

    public boolean removeOutputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeActuator(deviceToBeRemoved);
    }

    public boolean removeInputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeSensor(deviceToBeRemoved);
    }

    public boolean removeConnectivityDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeConnectivity(deviceToBeRemoved);
    }

    public void addDevice(Map<GenericDevice, Integer> device) {
        for (GenericDevice genericDevice : device.keySet()) {
            if (DeviceLibrary.INSTANCE.getGenericInputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addSensor(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericOutputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addActuator(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericConnectivityDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addConnectivity(genericDevice);
                }
            } else {
                throw new IllegalStateException("We are in great danger!!!");
            }
        }
    }

    public ObjectProperty<Platform> selectedPlatformProperty() {
        return platformProperty;
    }
}
