package io.makerplayground.device.actual;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize (using = PinTemplateDeserializer.class)
public class PinTemplate {
    private final String name;
    private final String codingName;
    private final VoltageLevel voltageLevel;
    private final List<PinFunction> function;
}
