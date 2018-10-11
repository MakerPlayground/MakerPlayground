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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Palmn on 7/15/2017.
 */
public class DevicePort {
    enum Type {
        WIRE, GROVE, MP, VIRTUAL
    }

    private String name;
    private List<String> alias;
    private Type type;
    private List<DevicePortFunction> function;
    private double vmin;
    private double vmax;
    private double x;
    private double y;

    @JsonCreator
    public DevicePort(@JsonProperty("name") String name, @JsonProperty("alias") List<String> alias, @JsonProperty("type")Type type
            , @JsonProperty("function") List<DevicePortFunction> function
            , @JsonProperty("v_min") double vmin, @JsonProperty("v_max") double vmax
            , @JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.name = name;
        this.alias = (alias == null) ? Collections.emptyList() : alias;
        this.type = type;
        this.function = function;
        this.vmin = vmin;
        this.vmax = vmax;
        this.x = x;
        this.y = y;

        // Port shouldn't contain more than one function with identical type e.g. GPIO_1 and GPIO_2 or GROVE_GPIO_SINGLE_1 and GROVE_GPIO_SINGLE_2
        // as it doesn't make sense and break our device mapping logic. Note that port can have multiple functions with different type
        // e.g. GPIO_1 and PWM_1 or GROVE_GPIO_SINGLE_1 and GROVE_ANALOG_SINGLE_1
        if (function.size() != function.stream().map(DevicePortFunction::getPeripheral).collect(Collectors.toSet()).size()) {
            throw new IllegalStateException("Port shouldn't contain more than one function with identical type");
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAlias() {
        return alias;
    }

    public Type getType() {
        return type;
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

    private static class DevicePortFunction {
        private Peripheral peripheral;
        private List<Peripheral> conflict;
        private PinType pinType;

        @JsonCreator
        public DevicePortFunction(@JsonProperty("type") Peripheral peripheral, @JsonProperty("conflict") List<Peripheral> conflict, @JsonProperty("pintype") PinType pinType) {
            this.peripheral = peripheral;
            this.conflict = (conflict == null) ? Collections.emptyList() : conflict;
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
