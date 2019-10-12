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

import lombok.Getter;
import lombok.ToString;

@Getter
public enum Platform {
    ARDUINO_AVR8("Arduino (Atmel AVR)", "arduino"),
    ARDUINO_ESP8266("Arduino (Espressif ESP8266)", "arduino"),
    ARDUINO_ESP32("Arduino (Espressif ESP32)", "arduino"),
    RASPBERRYPI("Raspberry Pi", "raspberrypi");

    Platform(String displayName, String libFolderName) {
        this.displayName = displayName;
        this.libFolderName = libFolderName;
    }

    private String displayName;
    private String libFolderName;
}
