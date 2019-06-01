package io.makerplayground.device.actual;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum DevicePortType {
    WIRE(Collections.emptyList(), 0),
    GROVE(List.of(new DevicePortPin(DevicePortPinType.SIGNAL_1, Color.web("#FEE600")),
            new DevicePortPin(DevicePortPinType.SIGNAL_2, Color.WHITE),
            new DevicePortPin(DevicePortPinType.POWER, Color.web("#EB2427")),
            new DevicePortPin(DevicePortPinType.GROUND, Color.BLACK))   // yellow, white, red, black
            , 2),
    MP(List.of(new DevicePortPin(DevicePortPinType.SIGNAL_1, Color.web("#FEE600")),
            new DevicePortPin(DevicePortPinType.SIGNAL_2, Color.WHITE),
            new DevicePortPin(DevicePortPinType.POWER, Color.web("#EB2427")),
            new DevicePortPin(DevicePortPinType.GROUND, Color.BLACK))   // yellow, white, red, black
            , 2),
    INEX_JST3(List.of(new DevicePortPin(DevicePortPinType.GROUND, Color.BLACK),
            new DevicePortPin(DevicePortPinType.SIGNAL_1, Color.WHITE),
            new DevicePortPin(DevicePortPinType.POWER, Color.web("#EB2427")))   // black, white, red
            , 2),
    INTERNAL(Collections.emptyList(), 0),
    RPI_CAMERA(List.of(new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.ANTIQUEWHITE),
            new DevicePortPin(DevicePortPinType.UNDEFINED, Color.BLUE))
            , 0.5),
    VIRTUAL(Collections.emptyList(), 0),     // TODO: this field is deprecated and will be removed after the device mapping algorithm is fixed to accept device without any port
    JR3_SERVO(List.of(new DevicePortPin(DevicePortPinType.GROUND, Color.BLACK),
            new DevicePortPin(DevicePortPinType.SIGNAL_1, Color.web("#EB2427")),
            new DevicePortPin(DevicePortPinType.POWER, Color.web("#FEE600")))
            , 2.2);

    private List<DevicePortPin> pinType;
    private double pinPitch;        // pin pitch of the connector in mm

    DevicePortType(List<DevicePortPin> pinType, double pinPitch) {
        this.pinType = pinType;
        this.pinPitch = pinPitch;
    }

    public List<DevicePortPin> getPinType() {
        return pinType;
    }

    public DevicePortPin getPinType(int i) {
        return pinType.get(i);
    }

    public Optional<Integer> getPinIndex(DevicePortPinType type) {
        for (int i=0; i<pinType.size(); i++) {
            if (pinType.get(i).getPinType() == type) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public int getPinCount() {
        return pinType.size();
    }

    public double getPinPitch() {
        return pinPitch;
    }
}
