package io.makerplayground.device.actual;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.project.ProjectDevice;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
@JsonDeserialize(using = PinDeserializer.class)
public class IntegratedPin extends Pin {
    private final String pinNameConnectTo;

    public IntegratedPin(String displayName, List<String> codingName, VoltageLevel voltageLevel, List<PinFunction> function, double x, double y, ProjectDevice ownerProjectDevice, String pinNameConnectTo) {
        super(displayName, codingName, voltageLevel, function, x, y, ownerProjectDevice);
        this.pinNameConnectTo = pinNameConnectTo;
    }
}
