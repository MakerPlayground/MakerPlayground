package io.makerplayground.ui.devicepanel;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIconViewModel {

    private final ProjectDevice device;
    private final Project project;
    //private final StringProperty name;

    public DevicePanelIconViewModel(ProjectDevice device, Project project) {
        this.device = device;
        this.project = project;
        //this.name = new SimpleStringProperty(device.getName());
        //this.name.addListener((observable, oldValue, newValue) -> this.device.setName(newValue));
    }

    public String getDeviceName() {
        return device.getGenericDevice().getName();
    }

    public String getName() {
        return device.getName();
    }

    public void setName(String name) {
        device.setName(name);
    }

    public StringProperty nameProperty() {
        return device.nameProperty();
    }

    public ProjectDevice getDevice() {
        return device;
    }

    // Check if current scene's name is duplicated with other scenes
    // return true when this name cannot be used
    public boolean isNameDuplicate(String newName) {
        for (ProjectDevice projectDevice : project.getAllDevice()) {
            if (projectDevice.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }
}
