package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.ProjectDevice;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class CompatibleDevice implements Comparable<CompatibleDevice> {
    private ActualDevice actualDevice;
    private ProjectDevice projectDevice;

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

    private static final Comparator<ActualDevice> comparator = Comparator.comparing(ActualDevice::getBrand).thenComparing(ActualDevice::getModel);

    @Override
    public int compareTo(CompatibleDevice that) {
        if (this.getProjectDevice().isPresent() && that.getProjectDevice().isPresent()) {
            return this.getProjectDevice().get().getName().compareTo(that.getProjectDevice().get().getName());
        }
        else if (this.getProjectDevice().isPresent() && that.getProjectDevice().isEmpty()) {
            return -1;
        }
        else if (this.getProjectDevice().isEmpty() && that.getProjectDevice().isPresent()) {
            return 1;
        }
        else {
            ActualDevice thisDevice = this.getActualDevice().orElseThrow();
            ActualDevice thatDevice = that.getActualDevice().orElseThrow();
            return comparator.compare(thisDevice, thatDevice);
        }
    }
}
