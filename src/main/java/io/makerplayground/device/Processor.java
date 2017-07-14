package io.makerplayground.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.PinType;
import io.makerplayground.helper.Platform;

import java.util.List;
import java.util.Map;

/**
 *
 */
@JsonDeserialize(using = ProcessorDeserializer.class)
public class Processor {
    private final String name;          // Board variant
    private final Platform platform;    // Software compatible
    private final Map<String, Map<Peripheral, PinType>> port;
    //private final Map<GenericDevice, Device> peripheral;

    public Processor(String name, Platform platform, Map<String, Map<Peripheral, PinType>> port) {
        this.name = name;
        this.platform = platform;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Map<String, Map<Peripheral, PinType>> getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Processor{" +
                "name='" + name + '\'' +
                ", platform=" + platform +
                ", port=" + port +
                '}';
    }
}
