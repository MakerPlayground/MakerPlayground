package io.makerplayground.helper;

import java.util.Collections;
import java.util.List;

/**
 * Created by tanyagorn on 7/14/2017.
 */
public enum ConnectionType {
    GPIO,
    ANALOG,
    INT,
    PWM,
    I2C,
    SPI,
    UART,
    POWER,
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
    GROVE_I2C;
}
