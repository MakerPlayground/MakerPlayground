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

import java.util.Set;

public enum PinFunction {
    VCC, GND, SCL, SDA, DIGITAL_IN, DIGITAL_OUT, ANALOG_IN, ANALOG_OUT, PWM, INTERRUPT, HW_SERIAL_IN, HW_SERIAL_OUT, SW_SERIAL_IN, SW_SERIAL_OUT, MOSI, MISO, SCK, AREF, NO_FUNCTION;

    public static final Set<PinFunction> FUNCTIONS_WITH_CODES = Set.of(DIGITAL_IN, DIGITAL_OUT, ANALOG_IN, ANALOG_OUT, PWM, INTERRUPT, HW_SERIAL_IN, HW_SERIAL_OUT, SW_SERIAL_IN, SW_SERIAL_OUT);

    public boolean isMultipleUsed() {
        switch (this) {
            case VCC:
            case GND:
            case SCL:
            case SDA:
            case NO_FUNCTION:
                return true;
            case DIGITAL_IN:
            case DIGITAL_OUT:
            case ANALOG_IN:
            case ANALOG_OUT:
            case PWM:
            case INTERRUPT:
            case HW_SERIAL_IN:
            case HW_SERIAL_OUT:
            case SW_SERIAL_IN:
            case SW_SERIAL_OUT:
                return false;
        }
        return false;
    }

    public PinFunction getOpposite() {
        switch (this) {
            case VCC:
                return VCC;
            case GND:
                return GND;
            case SCL:
                return SCL;
            case SDA:
                return SDA;
            case DIGITAL_IN:
                return DIGITAL_OUT;
            case DIGITAL_OUT:
                return DIGITAL_IN;
            case ANALOG_IN:
                return ANALOG_OUT;
            case ANALOG_OUT:
                return ANALOG_IN;
            case PWM:
                return PWM;
            case INTERRUPT:
                return INTERRUPT;
            case HW_SERIAL_IN:
                return HW_SERIAL_OUT;
            case HW_SERIAL_OUT:
                return HW_SERIAL_IN;
            case SW_SERIAL_IN:
                return SW_SERIAL_OUT;
            case SW_SERIAL_OUT:
                return SW_SERIAL_IN;
            case NO_FUNCTION:
                return NO_FUNCTION;
        }
        throw new IllegalStateException("");
    }
}
