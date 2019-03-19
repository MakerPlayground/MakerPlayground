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

/**
 * Created by tanyagorn on 7/14/2017.
 */
public enum ConnectionType {
    GPIO,
    ANALOG,
    INT,
    PWM,
    I2C,
    I2C1,
    SPI,
    UART,
    POWER,
    ONE_WIRE,
    NONE,

    /* Maker Playground */
    MP_INT_UART,
    MP_GPIO_SINGLE,
    MP_GPIO_DUAL,
    MP_PWM_SINGLE,
    MP_PWM_DUAL,
    MP_ANALOG_SINGLE,
    MP_ANALOG_DUAL,
    MP_I2C,
    MP_I2C1,

    /* Grove */
    GROVE_GPIO_SINGLE,
    GROVE_GPIO_DUAL,
    GROVE_PWM_SINGLE,
    GROVE_PWM_DUAL,
    GROVE_ANALOG_SINGLE,
    GROVE_ANALOG_DUAL,
    GROVE_I2C,
    GROVE_UART,

    /* INEX */
    INEX_GPIO,
    INEX_PWM,
    INEX_ANALOG,
    INEX_I2C,
    INEX_UART,
    INEX_WS2812,
    INEX_ONE_WIRE,
    INEX_SPI,

    /* JR3 Family */
    JR3_SERVO,

    /* RPI */
    RPI_CAMERA;

    public boolean isGPIO() {
        return (this == MP_GPIO_SINGLE) || (this == MP_GPIO_DUAL) || (this == GROVE_GPIO_SINGLE)
                || (this == GROVE_GPIO_DUAL) || (this == INEX_GPIO);
    }

    public boolean isPWM() {
        return (this == MP_PWM_SINGLE) || (this == MP_PWM_DUAL) || (this == GROVE_PWM_SINGLE)
                || (this == GROVE_PWM_DUAL) || (this == INEX_PWM);
    }

    public boolean isAnalog() {
        return (this == MP_ANALOG_SINGLE) || (this == MP_ANALOG_DUAL) || (this == GROVE_ANALOG_SINGLE)
                || (this == GROVE_ANALOG_DUAL) || (this == INEX_ANALOG);
    }

    public boolean isI2C() {
        return (this == MP_I2C) || (this == GROVE_I2C) || (this == INEX_I2C);
    }

    public boolean isUART() {
        return (this == GROVE_UART) || (this == INEX_UART);
    }
}
