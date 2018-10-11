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

package io.makerplayground.device.shared;

/**
 * An enum represent unit of a numeric value
 */
public enum Unit {
    //INPUT DEVICE
    TIME,METERPERSECSQUARE,HECTOPASCAL,METER,CELSIUS,FAHRENHEIT,KELVIN,RADIUSPERSEC,DEGREEPERSEC,DECIBEL,
    MICROTESLA,CENTIMETER,LUX,DEGREE,
    //OUTPUT DEVICE
    SECOND,MILLISECOND, WAV,NUMBER,
    //BASIC
    PERCENT,NOT_SPECIFIED,HERTZ,BPM;
    // TODO: add new unit

    @Override
    public String toString() {
        final String DEGREE  = "\u00b0";
        final String MICRO = "\u00B5";
        final String PERCENT = "\u0025";

        switch (this) {
            case TIME: return "Time";
            case METERPERSECSQUARE: return "m/s^2";
            case HECTOPASCAL: return "hPa";
            case METER: return "m";
            case CELSIUS: return DEGREE + "C";
            case FAHRENHEIT: return DEGREE + "F";
            case KELVIN: return DEGREE + "K";
            case RADIUSPERSEC: return "Rad/s";
            case DEGREEPERSEC: return "degree/s";
            case DECIBEL: return "dB";
            case MICROTESLA: return MICRO + "T";
            case CENTIMETER: return "cm";
            case LUX: return "lux";
            case DEGREE: return DEGREE;
            case SECOND: return "sec";
            case MILLISECOND: return "ms";
            case WAV: return "wav";
            case NUMBER: return "Number";
            case PERCENT: return PERCENT;
            case NOT_SPECIFIED: return "not specified";
            case HERTZ: return "Hz";
            case BPM: return "BPM";
            default: throw new IllegalStateException();
        }
    }

}
