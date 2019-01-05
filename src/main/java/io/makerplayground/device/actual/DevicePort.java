/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device.actual;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Palmn on 7/15/2017.
 */
public class DevicePort {
    private final String name;
    private final List<String> alias;
    private final DevicePortType type;
    private final DevicePortSubType subType;
    private final List<DevicePortFunction> function;
    private final double vmin;
    private final double vmax;
    private final double x;
    private final double y;
    private final double angle;
    private final DevicePort parent;

    @JsonCreator
    public DevicePort(@JsonProperty("name") String name, @JsonProperty("alias") List<String> alias, @JsonProperty("type") DevicePortType type
            , @JsonProperty("sub_type") DevicePortSubType subType
            , @JsonProperty("function") List<DevicePortFunction> function
            , @JsonProperty("v_min") double vmin, @JsonProperty("v_max") double vmax
            , @JsonProperty("x") double x, @JsonProperty("y") double y, @JsonProperty("angle") double angle) {
        this(name, alias, type, subType, function, vmin, vmax, x, y, angle, null);
    }

    public DevicePort(String name, List<String> alias, DevicePortType type, DevicePortSubType subType, List<DevicePortFunction> function, double vmin, double vmax, double x, double y, double angle, DevicePort parent) {
        this.name = name;
        this.alias = Objects.requireNonNullElse(alias, Collections.emptyList());
        this.type = type;
        this.subType = subType;
        this.function = function;
        this.vmin = vmin;
        this.vmax = vmax;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.parent = parent;

        // Port shouldn't contain more than one function with identical type e.g. GPIO_1 and GPIO_2 or GROVE_GPIO_SINGLE_1 and GROVE_GPIO_SINGLE_2
        // as it doesn't make sense and break our device mapping logic. Note that port can have multiple functions with different type
        // e.g. GPIO_1 and PWM_1 or GROVE_GPIO_SINGLE_1 and GROVE_ANALOG_SINGLE_1
        if (function.size() != function.stream().map(DevicePortFunction::getPeripheral).map(Peripheral::getConnectionType).distinct().count()) {
            throw new IllegalStateException("Port shouldn't contain more than one function with identical type [port name: " + this.name + "]");
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAlias() {
        return alias;
    }

    public DevicePortType getType() {
        return type;
    }

    public DevicePortSubType getSubType() {
        return subType;
    }

    public boolean hasConnectionType(ConnectionType connectionType) {
        return function.stream().map(DevicePortFunction::getPeripheral)
                .anyMatch(peripheral -> peripheral.getConnectionType() == connectionType);
    }

    public List<Peripheral> getPeripheral() {
        return function.stream().map(DevicePortFunction::getPeripheral).collect(Collectors.toList());
    }

    public Optional<Peripheral> getPeripheral(ConnectionType connectionType) {
        return function.stream().map(DevicePortFunction::getPeripheral)
                .filter(peripheral -> peripheral.getConnectionType() == connectionType)
                .findFirst();
    }

    public boolean hasPeripheral(Peripheral peripheral) {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPeripheral() == peripheral);
    }

    public List<Peripheral> getConflictPeripheral(Peripheral peripheral) {
        for (DevicePortFunction devicePortFunction : function) {
            if (devicePortFunction.getPeripheral().getConnectionType() == peripheral.getConnectionType()) {
                return devicePortFunction.getConflict();
            }
        }
        return Collections.emptyList();
    }

    public void addConflictPeripheral(Peripheral peripheral, List<Peripheral> conflictPeripheral) {
        for (DevicePortFunction devicePortFunction : function) {
            if (devicePortFunction.getPeripheral() == peripheral) {
                devicePortFunction.getConflict().addAll(conflictPeripheral);
            }
        }
    }

    public boolean isConflictedTo(DevicePort devicePort) {
        return function.stream().flatMap(devicePortFunction -> devicePortFunction.getConflict().stream())
                .anyMatch(devicePort::hasPeripheral);
    }

    public boolean isSupport(Peripheral p) {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPeripheral().getConnectionType() == p.getConnectionType());
    }

    public boolean isVcc() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.VCC);
    }

    public boolean isGnd() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.GND);
    }

    public boolean isSDA() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.I2C_SDA);
    }

    public boolean isSCL() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.I2C_SCL);
    }

    public boolean isMOSI() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.SPI_MOSI);
    }

    public boolean isMISO() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.SPI_MISO);
    }

    public boolean isSCK() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.SPI_SCK);
    }

    public boolean isSS() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.SPI_SS);
    }

    public boolean isRX() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.UART_RX);
    }

    public boolean isTX() {
        return function.stream().anyMatch(devicePortFunction -> devicePortFunction.getPinType() == PinType.UART_TX);
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

    public double getAngle() {
        return angle;
    }

    public DevicePort getParent() {
        return parent;
    }

    public static class DevicePortFunction {
        private Peripheral peripheral;
        private List<Peripheral> conflict;
        private PinType pinType;

        @JsonCreator
        public DevicePortFunction(@JsonProperty("type") Peripheral peripheral, @JsonProperty("conflict") List<Peripheral> conflict, @JsonProperty("pintype") PinType pinType) {
            this.peripheral = peripheral;
            this.conflict = (conflict == null) ? new ArrayList<>() : new ArrayList<>(conflict); // conflict list must be mutable
            this.pinType = pinType;
        }

        public Peripheral getPeripheral() {
            return peripheral;
        }

        public List<Peripheral> getConflict() {
            return conflict;
        }

        public PinType getPinType() {
            return pinType;
        }

        @Override
        public String toString() {
            return "DevicePortFunction{" +
                    "peripheral=" + peripheral +
                    ", conflict=" + conflict +
                    ", pinType=" + pinType +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DevicePort{" +
                "name='" + name + '\'' +
                ", alias=" + alias +
                ", type=" + type +
                ", function=" + function +
                ", vmin=" + vmin +
                ", vmax=" + vmax +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
