package io.makerplayground.device.actual;

import io.makerplayground.project.ProjectDevice;
import lombok.Getter;

import java.util.List;

public class IntegratedConnection extends Connection {

    @Getter
    private final String hostRefTo;

    IntegratedConnection(String name, ConnectionType type, List<Pin> pins, ProjectDevice ownerProjectDevice, String hostRefTo) {
        super(name, type, pins, ownerProjectDevice);
        this.hostRefTo = hostRefTo;
    }
}
