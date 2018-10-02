package io.makerplayground.helper;

/**
 * Created by nuntipat on 7/7/2017 AD.
 */
public enum Peripheral {
    NOT_CONNECTED(ConnectionType.NONE),

    GPIO_0(ConnectionType.GPIO),
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

    MP_PWM_DUAL_1(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_2(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_3(ConnectionType.MP_PWM_DUAL),
    MP_PWM_DUAL_4(ConnectionType.MP_PWM_DUAL),

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

    MP_I2C1_1(ConnectionType.MP_I2C),

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

    GROVE_PWM_DUAL_1(ConnectionType.GROVE_PWM_DUAL),

    GROVE_ANALOG_SINGLE_1(ConnectionType.GROVE_ANALOG_SINGLE),
    GROVE_ANALOG_SINGLE_2(ConnectionType.GROVE_ANALOG_SINGLE),
    GROVE_ANALOG_SINGLE_3(ConnectionType.GROVE_ANALOG_SINGLE),

    GROVE_ANALOG_DUAL_1(ConnectionType.GROVE_ANALOG_DUAL),
    GROVE_ANALOG_DUAL_2(ConnectionType.GROVE_ANALOG_DUAL),
    GROVE_ANALOG_DUAL_3(ConnectionType.GROVE_ANALOG_DUAL),

    GROVE_I2C_1(ConnectionType.GROVE_I2C),
    GROVE_I2C_2(ConnectionType.GROVE_I2C),

    POWER(ConnectionType.POWER);

    private ConnectionType connectionType;

    Peripheral(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public boolean isMPDual() {
       switch (this) {
           case MP_GPIO_DUAL_1:
           case MP_GPIO_DUAL_2:
           case MP_GPIO_DUAL_3:
           case MP_GPIO_DUAL_4:
           case MP_GPIO_DUAL_5:
           case MP_GPIO_DUAL_6:
           case MP_PWM_DUAL_1:
           case MP_PWM_DUAL_2:
           case MP_PWM_DUAL_3:
           case MP_PWM_DUAL_4:
           case MP_ANALOG_DUAL_1:
           case MP_ANALOG_DUAL_2:
               return true;
           default:
               return false;
       }
    }

    public boolean isGroveDual() {
        switch (this) {
            case GROVE_GPIO_DUAL_1:
            case GROVE_GPIO_DUAL_2:
            case GROVE_GPIO_DUAL_3:
            case GROVE_GPIO_DUAL_4:
            case GROVE_GPIO_DUAL_5:
            case GROVE_GPIO_DUAL_6:
            case GROVE_GPIO_DUAL_7:
            case GROVE_GPIO_DUAL_8:
            case GROVE_GPIO_DUAL_9:
            case GROVE_PWM_DUAL_1:
            case GROVE_ANALOG_DUAL_1:
            case GROVE_ANALOG_DUAL_2:
            case GROVE_ANALOG_DUAL_3:
                return true;
            default:
                return false;
        }
    }

    public boolean isI2C1() {
        switch (this) {
            case MP_I2C1_1:
                return true;
            default:
                return false;
        }
    }
}
