package io.makerplayground.device.actual;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;

public enum DevicePortType {
    WIRE(Collections.emptyList(), 1, 0),
    GROVE(List.of(Color.web("#FEE600"), Color.WHITE, Color.web("#EB2427"), Color.BLACK), 4, 2), // yellow, white, red, black
    MP(List.of(Color.web("#FEE600"), Color.WHITE, Color.web("#EB2427"), Color.BLACK), 4, 2),    // yellow, white, red, black
    INEX_JST3(List.of(Color.BLACK, Color.WHITE, Color.web("#EB2427")), 3, 2),                   // black, white, red
    INTERNAL(Collections.emptyList(), 0, 0),
    RPI_CAMERA(List.of(Color.BLUE, Color.ANTIQUEWHITE, Color.BLUE, Color.ANTIQUEWHITE,
            Color.BLUE, Color.ANTIQUEWHITE, Color.BLUE, Color.ANTIQUEWHITE, Color.BLUE,
            Color.ANTIQUEWHITE, Color.BLUE, Color.ANTIQUEWHITE, Color.BLUE, Color.ANTIQUEWHITE, Color.BLUE),
            15, 0.5),
    VIRTUAL(Collections.emptyList(), 0, 0);     // TODO: this field is deprecated and will be removed after the device mapping algorithm is fixed to accept device without any port

    private List<Color> wireColor;  // default color of wire
    private int pinCount;           // number of pin in the connector
    private double pinPitch;        // pin pitch of the connector in mm

    DevicePortType(List<Color> wireColor, int pinCount, double pinPitch) {
        this.wireColor = wireColor;
        this.pinCount = pinCount;
        this.pinPitch = pinPitch;
    }

    public List<Color> getWireColor() {
        return wireColor;
    }

    public int getPinCount() {
        return pinCount;
    }

    public double getPinPitch() {
        return pinPitch;
    }
}
