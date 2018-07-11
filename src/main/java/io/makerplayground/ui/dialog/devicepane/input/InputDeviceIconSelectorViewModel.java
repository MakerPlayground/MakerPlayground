package io.makerplayground.ui.dialog.devicepane.input;

import io.makerplayground.project.ProjectDevice;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceIconSelectorViewModel {
    private final ProjectDevice projectDevice;

    public InputDeviceIconSelectorViewModel(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
    }

    public String getImageName() {
        return this.projectDevice.getGenericDevice().getName();
    }

    public String getUserSettingName() {
        return this.projectDevice.getName();
    }

    public ProjectDevice getProjectDevice() {
        return projectDevice;
    }
}
