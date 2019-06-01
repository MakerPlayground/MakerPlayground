package io.makerplayground.device.actual;

import javafx.scene.paint.Color;

public class DevicePortPin {
    private final DevicePortPinType pinType;
    private final Color color;

    public DevicePortPin(DevicePortPinType pinType, Color color) {
        this.pinType = pinType;
        this.color = color;
    }

    public DevicePortPinType getPinType() {
        return pinType;
    }

    public Color getColor() {
        return color;
    }
}
