package io.makerplayground.ui.canvas.devicepane.output;

import io.makerplayground.project.ProjectDevice;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceIconSelectorViewModel {
    private final ProjectDevice projectDevice;

    public OutputDeviceIconSelectorViewModel(ProjectDevice projectDevice) {
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
