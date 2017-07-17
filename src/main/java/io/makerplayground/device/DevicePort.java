package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.PinType;

import java.util.List;
import java.util.Map;

/**
 * Created by Palmn on 7/15/2017.
 */
public class DevicePort {
    enum Type {
        WIRE    // TODO: add groove and other pin type
    }

    private String name;
    private Type type;
    private List<DevicePortFunction> function;
    private double vmin;
    private double vmax;
    private double x;
    private double y;

    @JsonCreator
    public DevicePort(@JsonProperty("name") String name, @JsonProperty("type")Type type
            , @JsonProperty("function") List<DevicePortFunction> function
            , @JsonProperty("v_min") double vmin, @JsonProperty("v_max") double vmax
            , @JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.name = name;
        this.type = type;
        this.function = function;
        this.vmin = vmin;
        this.vmax = vmax;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<DevicePortFunction> getFunction() {
        return function;
    }

    public double getVmin() {
        return vmin;
    }

    public double getVmax() {
        return vmax;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private static class DevicePortFunction {
        private Peripheral peripheral;
        private PinType pinType;

        @JsonCreator
        public DevicePortFunction(@JsonProperty("type") Peripheral peripheral, @JsonProperty("pintype") PinType pinType) {
            this.peripheral = peripheral;
            this.pinType = pinType;
        }

        public Peripheral getPeripheral() {
            return peripheral;
        }

        public PinType getPinType() {
            return pinType;
        }

        @Override
        public String toString() {
            return "DevicePortFunction{" +
                    "peripheral=" + peripheral +
                    ", pinType=" + pinType +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DevicePort{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", function=" + function +
                ", vmin=" + vmin +
                ", vmax=" + vmax +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
