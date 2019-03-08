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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nuntipat on 7/7/2017 AD.
 */
public enum Peripheral {
    NOT_CONNECTED(ConnectionType.NONE),

    GPIO_1(ConnectionType.GPIO),
    GPIO_2(ConnectionType.GPIO),
    GPIO_3(ConnectionType.GPIO),
    GPIO_4(ConnectionType.GPIO),
    GPIO_5(ConnectionType.GPIO),
    GPIO_6(ConnectionType.GPIO),
    GPIO_7(ConnectionType.GPIO),
    GPIO_8(ConnectionType.GPIO),
    GPIO_9(ConnectionType.GPIO),
    GPIO_10(ConnectionType.GPIO),
    GPIO_11(ConnectionType.GPIO),
    GPIO_12(ConnectionType.GPIO),
    GPIO_13(ConnectionType.GPIO),
    GPIO_14(ConnectionType.GPIO),
    GPIO_15(ConnectionType.GPIO),
    GPIO_16(ConnectionType.GPIO),
    GPIO_17(ConnectionType.GPIO),
    GPIO_18(ConnectionType.GPIO),
    GPIO_19(ConnectionType.GPIO),
    GPIO_20(ConnectionType.GPIO),
    GPIO_21(ConnectionType.GPIO),
    GPIO_22(ConnectionType.GPIO),
    GPIO_23(ConnectionType.GPIO),
    GPIO_24(ConnectionType.GPIO),
    GPIO_25(ConnectionType.GPIO),
    GPIO_26(ConnectionType.GPIO),
    GPIO_27(ConnectionType.GPIO),

    PWM_1(ConnectionType.PWM),
    PWM_2(ConnectionType.PWM),
    PWM_3(ConnectionType.PWM),
    PWM_4(ConnectionType.PWM),
    PWM_5(ConnectionType.PWM),
    PWM_6(ConnectionType.PWM),
    PWM_7(ConnectionType.PWM),
    PWM_8(ConnectionType.PWM),
    PWM_9(ConnectionType.PWM),
    PWM_10(ConnectionType.PWM),
    PWM_11(ConnectionType.PWM),
    PWM_12(ConnectionType.PWM),
    PWM_13(ConnectionType.PWM),
    PWM_14(ConnectionType.PWM),
    PWM_15(ConnectionType.PWM),
    PWM_16(ConnectionType.PWM),
    PWM_17(ConnectionType.PWM),
    PWM_18(ConnectionType.PWM),
    PWM_19(ConnectionType.PWM),
    PWM_20(ConnectionType.PWM),
    PWM_21(ConnectionType.PWM),
    PWM_22(ConnectionType.PWM),
    PWM_23(ConnectionType.PWM),
    PWM_24(ConnectionType.PWM),
    PWM_25(ConnectionType.PWM),
    PWM_26(ConnectionType.PWM),
    PWM_27(ConnectionType.PWM),

    INT_1(ConnectionType.INT),
    INT_2(ConnectionType.INT),
    INT_3(ConnectionType.INT),

    I2C_0(ConnectionType.I2C),
    I2C_1(ConnectionType.I2C),
    I2C1_1(ConnectionType.I2C1),    // TODO: should we change I2C1_1 to I2C_2?

    SPI_0(ConnectionType.SPI),
    SPI_1(ConnectionType.SPI),

    ONE_WIRE_1(ConnectionType.ONE_WIRE),

    UART_0(ConnectionType.UART),
    UART_1(ConnectionType.UART),
    UART_2(ConnectionType.UART),
    UART_3(ConnectionType.UART),
    UART_4(ConnectionType.UART),

    ANALOG_0(ConnectionType.ANALOG),
    ANALOG_1(ConnectionType.ANALOG),
    ANALOG_2(ConnectionType.ANALOG),
    ANALOG_3(ConnectionType.ANALOG),
    ANALOG_4(ConnectionType.ANALOG),
    ANALOG_5(ConnectionType.ANALOG),
    ANALOG_6(ConnectionType.ANALOG),
    ANALOG_7(ConnectionType.ANALOG),

    MP_INT_UART_1(ConnectionType.MP_INT_UART),

    MP_GPIO_SINGLE_1(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_2(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_3(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_4(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_5(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_6(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_7(ConnectionType.MP_GPIO_SINGLE),
    MP_GPIO_SINGLE_8(ConnectionType.MP_GPIO_SINGLE),

    MP_GPIO_DUAL_1(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_2(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_3(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_4(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_5(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_6(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_7(ConnectionType.MP_GPIO_DUAL),
    MP_GPIO_DUAL_8(ConnectionType.MP_GPIO_DUAL),

    MP_PWM_SINGLE_1(ConnectionType.MP_PWM_SINGLE),
    MP_PWM_SINGLE_2(ConnectionType.MP_PWM_SINGLE),
    MP_PWM_SINGLE_3(ConnectionType.MP_PWM_SINGLE),
    MP_PWM_SINGLE_4(ConnectionType.MP_PWM_SINGLE),
    MP_PWM_SINGLE_5(ConnectionType.MP_PWM_SINGLE),
    MP_PWM_SINGLE_6(ConnectionType.MP_PWM_SINGLE),

    MP_PWM_DUAL_1(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_2(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_3(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_4(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_5(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_6(ConnectionType.MP_PWM_DUAL),

    MP_ANALOG_SINGLE_1(ConnectionType.MP_ANALOG_SINGLE),
    MP_ANALOG_SINGLE_2(ConnectionType.MP_ANALOG_SINGLE),
    MP_ANALOG_SINGLE_3(ConnectionType.MP_ANALOG_SINGLE),

    MP_ANALOG_DUAL_1(ConnectionType.MP_ANALOG_DUAL),
    MP_ANALOG_DUAL_2(ConnectionType.MP_ANALOG_DUAL),
    MP_ANALOG_DUAL_3(ConnectionType.MP_ANALOG_DUAL),

    MP_I2C_1(ConnectionType.MP_I2C),
    MP_I2C_2(ConnectionType.MP_I2C),
    MP_I2C_3(ConnectionType.MP_I2C),
    MP_I2C_4(ConnectionType.MP_I2C),

//    MP_I2C1_1(ConnectionType.MP_I2C1),

    GROVE_GPIO_SINGLE_1(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_2(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_3(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_4(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_5(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_6(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_7(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_8(ConnectionType.GROVE_GPIO_SINGLE),
    GROVE_GPIO_SINGLE_9(ConnectionType.GROVE_GPIO_SINGLE),

    GROVE_GPIO_DUAL_1(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_2(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_3(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_4(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_5(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_6(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_7(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_8(ConnectionType.GROVE_GPIO_DUAL),
    GROVE_GPIO_DUAL_9(ConnectionType.GROVE_GPIO_DUAL),

    GROVE_PWM_SINGLE_1(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_2(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_3(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_4(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_5(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_6(ConnectionType.GROVE_PWM_SINGLE),
    GROVE_PWM_SINGLE_7(ConnectionType.GROVE_PWM_SINGLE),

    GROVE_PWM_DUAL_1(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_2(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_3(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_4(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_5(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_6(ConnectionType.GROVE_PWM_DUAL),
    GROVE_PWM_DUAL_7(ConnectionType.GROVE_PWM_DUAL),

    GROVE_ANALOG_SINGLE_1(ConnectionType.GROVE_ANALOG_SINGLE),
    GROVE_ANALOG_SINGLE_2(ConnectionType.GROVE_ANALOG_SINGLE),
    GROVE_ANALOG_SINGLE_3(ConnectionType.GROVE_ANALOG_SINGLE),
    GROVE_ANALOG_SINGLE_4(ConnectionType.GROVE_ANALOG_SINGLE),

    GROVE_ANALOG_DUAL_1(ConnectionType.GROVE_ANALOG_DUAL),
    GROVE_ANALOG_DUAL_2(ConnectionType.GROVE_ANALOG_DUAL),
    GROVE_ANALOG_DUAL_3(ConnectionType.GROVE_ANALOG_DUAL),
    GROVE_ANALOG_DUAL_4(ConnectionType.GROVE_ANALOG_DUAL),

    GROVE_UART_1(ConnectionType.GROVE_UART),

    GROVE_I2C_1(ConnectionType.GROVE_I2C),
    GROVE_I2C_2(ConnectionType.GROVE_I2C),
    GROVE_I2C_3(ConnectionType.GROVE_I2C),

    INEX_GPIO_1(ConnectionType.INEX_GPIO),
    INEX_GPIO_2(ConnectionType.INEX_GPIO),
    INEX_GPIO_3(ConnectionType.INEX_GPIO),
    INEX_GPIO_4(ConnectionType.INEX_GPIO),
    INEX_GPIO_5(ConnectionType.INEX_GPIO),
    INEX_GPIO_6(ConnectionType.INEX_GPIO),
    INEX_GPIO_11(ConnectionType.INEX_GPIO),
    INEX_GPIO_12(ConnectionType.INEX_GPIO),
    INEX_GPIO_13(ConnectionType.INEX_GPIO),
    INEX_GPIO_14(ConnectionType.INEX_GPIO),
    INEX_GPIO_15(ConnectionType.INEX_GPIO),
    INEX_GPIO_17(ConnectionType.INEX_GPIO),
    INEX_GPIO_18(ConnectionType.INEX_GPIO),
    INEX_GPIO_20(ConnectionType.INEX_GPIO),
    INEX_GPIO_22(ConnectionType.INEX_GPIO),
    INEX_GPIO_23(ConnectionType.INEX_GPIO),
    INEX_GPIO_24(ConnectionType.INEX_GPIO),
    INEX_GPIO_25(ConnectionType.INEX_GPIO),
    INEX_GPIO_26(ConnectionType.INEX_GPIO),
    INEX_GPIO_27(ConnectionType.INEX_GPIO),

    INEX_PWM_1(ConnectionType.INEX_PWM),
    INEX_PWM_2(ConnectionType.INEX_PWM),
    INEX_PWM_3(ConnectionType.INEX_PWM),
    INEX_PWM_4(ConnectionType.INEX_PWM),
    INEX_PWM_5(ConnectionType.INEX_PWM),
    INEX_PWM_6(ConnectionType.INEX_PWM),
    INEX_PWM_11(ConnectionType.INEX_PWM),
    INEX_PWM_13(ConnectionType.INEX_PWM),
    INEX_PWM_14(ConnectionType.INEX_PWM),
    INEX_PWM_15(ConnectionType.INEX_PWM),
    INEX_PWM_24(ConnectionType.INEX_PWM),
    INEX_PWM_25(ConnectionType.INEX_PWM),
    INEX_PWM_26(ConnectionType.INEX_PWM),
    INEX_PWM_27(ConnectionType.INEX_PWM),

    INEX_ONE_WIRE_1(ConnectionType.INEX_ONE_WIRE),

    INEX_ANALOG_1(ConnectionType.INEX_ANALOG),
    INEX_ANALOG_6(ConnectionType.INEX_ANALOG),

    INEX_UART_0(ConnectionType.INEX_UART),

    INEX_I2C_1(ConnectionType.INEX_I2C),
    INEX_SPI_1(ConnectionType.INEX_SPI),

    INEX_WS2812_1(ConnectionType.INEX_WS2812),

    JR3_SERVO_PWM_1(ConnectionType.JR3_SERVO),
    JR3_SERVO_PWM_2(ConnectionType.JR3_SERVO),

    RPI_CAMERA(ConnectionType.RPI_CAMERA),

    POWER(ConnectionType.POWER);


    private ConnectionType connectionType;

    Peripheral(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public static List<Peripheral> values(ConnectionType connectionType) {
        return Stream.of(values()).filter(peripheral -> peripheral.getConnectionType() == connectionType).collect(Collectors.toList());
    }

    public boolean isSingle() {
        return isMPSingle() || isGroveSingle() || isInexSingle();
    }

    public boolean isMPSingle() {
        switch (this.getConnectionType()) {
            case MP_GPIO_SINGLE:
            case MP_PWM_SINGLE:
            case MP_ANALOG_SINGLE:
                return true;
            default:
                return false;
        }
    }

    public boolean isGroveSingle() {
        switch (this.getConnectionType()) {
            case GROVE_GPIO_SINGLE:
            case GROVE_PWM_SINGLE:
            case GROVE_ANALOG_SINGLE:
                return true;
            default:
                return false;
        }
    }

    public boolean isInexSingle() {
        switch (this.getConnectionType()) {
            case INEX_GPIO:
            case INEX_PWM:
            case INEX_ANALOG:
            case INEX_WS2812:
            case INEX_I2C:
            case INEX_UART:
                return true;
            default:
                return false;
        }
    }

    public boolean isDual() {
        return isMPDual() || isGroveDual();
    }

    public boolean isMPDual() {
       switch (this.getConnectionType()) {
           case MP_GPIO_DUAL:
           case MP_PWM_DUAL:
           case MP_ANALOG_DUAL:
               return true;
           default:
               return false;
       }
    }

    public boolean isGroveDual() {
        switch (this.getConnectionType()) {
            case GROVE_GPIO_DUAL:
            case GROVE_ANALOG_DUAL:
            case GROVE_PWM_DUAL:
                return true;
            default:
                return false;
        }
    }

    public boolean isI2C1() {
        switch (this.getConnectionType()) {
            case MP_I2C1:
            case I2C1:
                return true;
            default:
                return false;
        }
    }

    public boolean isI2C() {
        switch (this.getConnectionType()) {
            case I2C:
            case MP_I2C:
            case GROVE_I2C:
            case INEX_I2C:
                return true;
            default:
                return false;
        }
    }
}
