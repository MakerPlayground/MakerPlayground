package io.makerplayground.helper;

/**
 * Created by nuntipat on 7/7/2017 AD.
 */
public enum Peripheral {
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

    PWM_1(ConnectionType.PWM),
    PWM_2(ConnectionType.PWM),
    PWM_3(ConnectionType.PWM),
    PWM_4(ConnectionType.PWM),
    PWM_5(ConnectionType.PWM),
    PWM_6(ConnectionType.PWM),

    INT_1(ConnectionType.INT),
    INT_2(ConnectionType.INT),
    INT_3(ConnectionType.INT),

    I2C_1(ConnectionType.I2C),
    I2C_2(ConnectionType.I2C),
    I2C_3(ConnectionType.I2C),
    I2C_4(ConnectionType.I2C),

    SPI_1(ConnectionType.SPI),
    SPI_2(ConnectionType.SPI),
    SPI_3(ConnectionType.SPI),
    SPI_4(ConnectionType.SPI),

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

    POWER(ConnectionType.POWER);

    private ConnectionType connectionType;

    Peripheral(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

}
