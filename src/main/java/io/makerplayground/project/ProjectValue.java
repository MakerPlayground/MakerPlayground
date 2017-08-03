package io.makerplayground.project;

import io.makerplayground.device.Value;

/**
 * Created by USER on 12-Jul-17.
 */
public class ProjectValue {
    private final ProjectDevice device;
    private final Value value;

    public ProjectValue(ProjectDevice device, Value value) {
        this.device = device;
        this.value = value;
    }

    public ProjectDevice getDevice() {
        return device;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ProjectValue{" +
                "device=" + device.getName() +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectValue that = (ProjectValue) o;

        if (device != null ? !device.equals(that.device) : that.device != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = device != null ? device.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
