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

package io.makerplayground.util;

public class AzureCognitiveServices implements AzureResource {
    private final String name;
    private final String location;
    private String key1;
    private String key2;

    public AzureCognitiveServices(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public AzureCognitiveServices(String name, String location, String key1, String key2) {
        this.name = name;
        this.location = location;
        this.key1 = key1;
        this.key2 = key2;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }
}
