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

package io.makerplayground.generator.source;

import io.makerplayground.project.Project;

public class SourceCode {

    public static SourceCodeResult generate(Project project, boolean isInteractive) {
        if (!isInteractive) {
            switch (project.getSelectedPlatform()) {
                case ARDUINO_AVR8:
                case ARDUINO_ESP8266:
                case ARDUINO_ESP32:
                case ARDUINO_ATSAMD51:
                    return ArduinoUploadCode.generateCode(project);
                case RASPBERRYPI:
                    return RpiPythonUploadCode.generateCode(project);
            }
        } else {
            switch (project.getSelectedPlatform()) {
                case ARDUINO_AVR8:
                case ARDUINO_ESP8266:
                case ARDUINO_ESP32:
                case ARDUINO_ATSAMD51:
                    return ArduinoInteractiveCode.generateCode(project);
                case RASPBERRYPI:
                    return RpiPythonInteractiveCode.generateCode(project);
                default:
                    throw new IllegalStateException("Not Support code for platform: " + project.getSelectedPlatform());
            }
        }
        throw new IllegalStateException("Not Support code for platform: " + project.getSelectedPlatform());
    }
}
