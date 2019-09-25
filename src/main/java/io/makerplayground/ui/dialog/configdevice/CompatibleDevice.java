package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.ProjectDevice;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class CompatibleDevice implements Comparable<CompatibleDevice> {
    private ActualDevice actualDevice;
    private ProjectDevice projectDevice;

    private static final Comparator<ProjectDevice> PROJECT_DEVICE_COMPARATOR = Comparator.comparing(ProjectDevice::getName);
    private static final Comparator<ActualDevice> ACTUAL_DEVICE_COMPARATOR = Comparator.comparing(ActualDevice::getBrand).thenComparing(ActualDevice::getModel).thenComparing(ActualDevice::getId);

    public CompatibleDevice(ActualDevice actualDevice) {
        this.actualDevice = actualDevice;
    }

    public CompatibleDevice(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
    }

    public Optional<ActualDevice> getActualDevice() {
        return actualDevice != null ? Optional.of(actualDevice) : Optional.empty();
    }

    public Optional<ProjectDevice> getProjectDevice() {
        return projectDevice != null ? Optional.of(projectDevice) : Optional.empty();
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

    @Override
    public int compareTo(CompatibleDevice that) {
        if (projectDevice != null && that.projectDevice != null) {
            return PROJECT_DEVICE_COMPARATOR.compare(projectDevice, that.projectDevice);
        }
        else if (actualDevice != null && that.actualDevice != null) {
            return ACTUAL_DEVICE_COMPARATOR.compare(actualDevice, that.actualDevice);
        }
        else if (projectDevice != null) { // that.projectDevice always null in this case
            return -1;
        }
        else if (that.projectDevice != null) { // this.projectDevice always null in this case
            return 1;
        }
        throw new IllegalStateException("CompatibleDevice must contains neither projectDevice nor actualDevice");
    }
}
