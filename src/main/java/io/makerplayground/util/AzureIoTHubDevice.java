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

public class AzureIoTHubDevice implements AzureResource {
    private final String deviceId;
    private String connectionString;

    public AzureIoTHubDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    public AzureIoTHubDevice(String deviceId, String connectionString) {
        this.deviceId = deviceId;
        this.connectionString = connectionString;
    }

    @Override
    public String getName() {
        return deviceId;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
