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

import java.util.List;

@Getter
public enum CloudPlatform {
    BLYNK("Blynk", List.of("Auth Token", "Wifi's SSID", "Wifi's Password"), "MP_BLYNK"),
    NETPIE("NETPIE", List.of("App Id", "Key", "Secret", "Alias", "Wifi's SSID", "Wifi's Password"), "MP_NETPIE"),
    REST("Rest API", List.of("Wifi's SSID", "Wifi's Password"), "MP_REST"),
    AZURE("Azure", List.of("Wifi's SSID", "Wifi's Password"), "MP_AZURE");

    CloudPlatform(String displayName, List<String> parameter, String libName) {
        this.displayName = displayName;
        this.parameter = parameter;
        this.libName = libName;
    }

    private final String displayName;
    @ToString.Exclude private final List<String> parameter;
    @ToString.Exclude private final String libName;
}
