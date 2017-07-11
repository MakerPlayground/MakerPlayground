package io.makerplayground.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.helper.Port;

import java.util.List;
import java.util.Map;

/**
 *
 */
@JsonDeserialize(using = ProcessorDeserializer.class)
public class Processor {
    private final String name;
    private final Platform platform;
    private final Map<Port, List<String>> port;
    private final Map<GenericDevice, Device> peripheral;

    public Processor(String name, Platform platform, Map<Port, List<String>> port, Map<GenericDevice, Device> peripheral) {
        this.name = name;
        this.platform = platform;
        this.port = port;
        this.peripheral = peripheral;
    }

    public String getName() {
        return name;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Map<Port, List<String>> getPort() {
        return port;
    }

    public Map<GenericDevice, Device> getPeripheral() {
        return peripheral;
    }
}
