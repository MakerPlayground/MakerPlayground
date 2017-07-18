package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Value;

/**
 * Created by USER on 12-Jul-17.
 */
@JsonDeserialize (using = ProjectValueDeserializer.class)
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
}
