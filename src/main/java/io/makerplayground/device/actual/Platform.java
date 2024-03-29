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

import io.makerplayground.upload.UploadMode;
import lombok.Getter;

import java.util.List;

@Getter
public enum Platform {
    ARDUINO_AVR8("Arduino (Atmel AVR)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    ARDUINO_ESP8266("Arduino (Espressif ESP8266)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    ARDUINO_ESP32("Arduino (Espressif ESP32)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    ARDUINO_ATSAMD21("Arduino (Atmel SAMD21)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    ARDUINO_ATSAMD51("Arduino (Atmel SAMD51)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    ARDUINO_K210("Arduino (K210)", "arduino", List.of(UploadMode.SERIAL_PORT)),
    MICROPYTHON_ESP32("MicroPython (ESP32)", "micropython", List.of(UploadMode.SERIAL_PORT)),
    MICROPYTHON_K210("MicroPython (K210)", "micropython", List.of(UploadMode.SERIAL_PORT)),
    RASPBERRYPI("Raspberry Pi", "raspberrypi", List.of(UploadMode.RPI_ON_NETWORK));

    Platform(String displayName, String libFolderName, List<UploadMode> supportUploadModes) {
        this.displayName = displayName;
        this.libFolderName = libFolderName;
        this.supportUploadModes = supportUploadModes;
    }

    private String displayName;
    private String libFolderName;
    private List<UploadMode> supportUploadModes;

    public boolean isArduino() {
        return this == ARDUINO_AVR8 || this == ARDUINO_ESP32 || this == ARDUINO_ESP8266 || this == ARDUINO_ATSAMD21 || this == ARDUINO_ATSAMD51 || this == ARDUINO_K210;
    }

    public boolean isMicroPython() {
        return this == MICROPYTHON_ESP32 || this == MICROPYTHON_K210;
    }
}
