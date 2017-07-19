package io.makerplayground.helper;

/**
 * Created by nuntipat on 7/7/2017 AD.
 */
public enum Peripheral {
    GPIO_1, GPIO_2, GPIO_3, GPIO_4, GPIO_5, GPIO_6, GPIO_7, GPIO_8, GPIO_9, GPIO_10, GPIO_11, GPIO_12, GPIO_13, GPIO_14,
    PWM_1, PWM_2, PWM_3, PWM_4, PWM_5, PWM_6,
    INT_1, INT_2, INT_3,
    I2C_1, I2C_2, I2C_3, I2C_4,
    SPI_1, SPI_2, SPI_3, SPI_4,
    UART_1, UART_2, UART_3, UART_4,
    POWER;


    public ConnectionType getType() {
        if (Peripheral.isGPIO(this))
            return ConnectionType.GPIO;
        else if (Peripheral.isPWM(this))
            return ConnectionType.PWM;
        else if (Peripheral.isI2C(this))
            return ConnectionType.I2C;
        else if (Peripheral.isINT(this))
            return ConnectionType.INT;
        else if (Peripheral.isSPI(this))
            return ConnectionType.SPI;
        else if (Peripheral.isUART(this))
            return ConnectionType.UART;
        else
            return null;
    }



    public static boolean isGPIO(Peripheral p) {
        return (p == GPIO_1) | (p == GPIO_2) | (p == GPIO_3);
    }

    public static boolean isPWM(Peripheral p) {
        return (p == PWM_1) | (p == PWM_2) | (p == PWM_3) | (p == PWM_4) | (p == PWM_5);
    }

    public static boolean isINT(Peripheral p) {
        return (p == INT_1) | (p == INT_2) | (p == INT_3);
    }

    public static boolean isI2C(Peripheral p) {
        return (p == I2C_1) | (p == I2C_2) | (p == I2C_3) | (p == I2C_4);
    }

    public static boolean isUART(Peripheral p) {
        return (p == UART_1) | (p == UART_2) | (p == UART_3) | (p == UART_4);
    }

    public static boolean isSPI(Peripheral p) {
        return (p == SPI_1) | (p == SPI_2) | (p == SPI_3) | (p == SPI_4);
    }

}
