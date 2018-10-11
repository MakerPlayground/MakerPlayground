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

package io.makerplayground.device.actual;

public enum Platform {
    MP_ARDUINO("Maker Playground's Arduino Kit", "arduino"),
    MP_ESP32("Maker Playground's ESP32 Kit", "arduino"),
    ARDUINO("Arduino", "arduino"),
    GROVE_ARDUINO("Grove for Arduino", "arduino"),
    ESP32("ESP32", "arduino");

    private String displayName;
    private String libFolderName;

    Platform(String displayName, String libFolderName) {
        this.displayName = displayName;
        this.libFolderName = libFolderName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLibraryFolderName() {
        return libFolderName;
    }
}
