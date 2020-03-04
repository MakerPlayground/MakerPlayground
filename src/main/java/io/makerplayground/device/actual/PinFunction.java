/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

public enum PinFunction {
    VCC, GND,
    SCL, SDA,
    SCL1, SDA1,
    DIGITAL_IN, DIGITAL_OUT,
    ANALOG_IN, ANALOG_OUT,
    PWM_OUT, PWM_IN,
    INTERRUPT_LOW, INTERRUPT_HIGH, INTERRUPT_CHANGE, INTERRUPT_RISING, INTERRUPT_FALLING,
    HW_SERIAL_RX, HW_SERIAL_TX,
    HW_SERIAL_RX1, HW_SERIAL_TX1,
    SW_SERIAL_RX, SW_SERIAL_TX,
    HW_OR_SW_SERIAL_RX, HW_OR_SW_SERIAL_TX,
    MOSI, MISO, SCK,
    MOSI1, MISO1, SCK1,
    AREF, NO_FUNCTION,
    RS485_Negative, RS485_Positive,
    CSI1, CSI2, CSI3, CSI4, CSI5, CSI6, CSI7, CSI8, CSI9, CSI10, CSI11, CSI12, CSI13, CSI14, CSI15;

    public boolean isSingleUsed() {
        switch (this) {
            case VCC:
            case GND:
            case MOSI:
            case MOSI1:
            case MISO:
            case MISO1:
            case SCK:
            case SCK1:
            case SCL:
            case SCL1:
            case SDA:
            case SDA1:
            case RS485_Negative:
            case RS485_Positive:
            case NO_FUNCTION:
                return false;
            case INTERRUPT_LOW:
            case INTERRUPT_HIGH:
            case INTERRUPT_CHANGE:
            case INTERRUPT_RISING:
            case INTERRUPT_FALLING:
            case DIGITAL_IN:
            case DIGITAL_OUT:
            case ANALOG_IN:
            case ANALOG_OUT:
            case PWM_OUT:
            case PWM_IN:
            case HW_SERIAL_RX:
            case HW_SERIAL_TX:
            case SW_SERIAL_RX:
            case SW_SERIAL_TX:
            case AREF:
            case HW_OR_SW_SERIAL_RX:
            case HW_OR_SW_SERIAL_TX:
                return true;
        }
        return true;
    }

    public List<PinFunction> getPossibleConsume() {
        switch (this) {
            case VCC:
                return List.of(VCC);
            case GND:
                return List.of(GND);
            case SCL:
                return List.of(SCL);
            case SDA:
                return List.of(SDA);
            case SCL1:
                return List.of(SCL1);
            case SDA1:
                return List.of(SDA1);
            case DIGITAL_IN:
                return List.of(DIGITAL_OUT);
            case DIGITAL_OUT:
                return List.of(DIGITAL_IN);
            case ANALOG_IN:
                return List.of(ANALOG_OUT);
            case ANALOG_OUT:
                return List.of(ANALOG_IN);
            case PWM_OUT:
                return List.of(PWM_IN);
            case PWM_IN:
                return List.of(PWM_OUT);
            case INTERRUPT_LOW:
                return List.of(INTERRUPT_LOW);
            case INTERRUPT_HIGH:
                return List.of(INTERRUPT_HIGH);
            case INTERRUPT_CHANGE:
                return List.of(INTERRUPT_CHANGE);
            case INTERRUPT_RISING:
                return List.of(INTERRUPT_RISING);
            case INTERRUPT_FALLING:
                return List.of(INTERRUPT_FALLING);
            case HW_SERIAL_RX:
                return List.of(HW_SERIAL_TX);
            case HW_SERIAL_TX:
                return List.of(HW_SERIAL_RX);
            case SW_SERIAL_RX:
                return List.of(SW_SERIAL_TX);
            case SW_SERIAL_TX:
                return List.of(SW_SERIAL_RX);
            case HW_OR_SW_SERIAL_RX:
                return List.of(HW_SERIAL_TX, SW_SERIAL_TX);
            case HW_OR_SW_SERIAL_TX:
                return List.of(HW_SERIAL_RX, SW_SERIAL_RX);
            case MOSI:
                return List.of(MOSI);
            case MISO:
                return List.of(MISO);
            case SCK:
                return List.of(SCK);
            case MOSI1:
                return List.of(MOSI1);
            case MISO1:
                return List.of(MISO1);
            case SCK1:
                return List.of(SCK1);
            case AREF:
                return List.of();
            case RS485_Negative:
                return List.of(RS485_Negative);
            case RS485_Positive:
                return List.of(RS485_Positive);
            case NO_FUNCTION:
                return List.of(PinFunction.values());
        }
        throw new IllegalStateException("");
    }

    public final static List<PinFunction> HWSerialList = List.of(HW_SERIAL_RX, HW_SERIAL_RX1, HW_SERIAL_TX, HW_SERIAL_TX1);

    public boolean isHWSerial() {
        return HWSerialList.contains(this);
    }
}
