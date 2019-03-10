package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.ProjectDevice;

import java.util.Objects;

public class CompatibleDevice {
    private ActualDevice actualDevice;
    private ProjectDevice projectDevice;

    public CompatibleDevice(ActualDevice actualDevice) {
        this.actualDevice = actualDevice;
    }

    public CompatibleDevice(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
    }

    public ActualDevice getActualDevice() {
        return actualDevice;
    }

    public ProjectDevice getProjectDevice() {
        return projectDevice;
    }

    @Override
    public String toString() {
        if (actualDevice != null) {
            return actualDevice.getBrand() + " " + actualDevice.getModel();
        } else {
            return "Use the same device as " + projectDevice.getName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompatibleDevice that = (CompatibleDevice) o;
        return ((actualDevice != null) && (actualDevice == that.actualDevice))
                || ((projectDevice != null) && (projectDevice == that.projectDevice));
    }

    @Override
    public int hashCode() {
        if (actualDevice != null) {
            return Objects.hash(actualDevice);
        } else {
            return Objects.hash(projectDevice);
        }
    }
}