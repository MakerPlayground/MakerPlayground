/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.devicemonitortab;

public class LogItem {

    private final LogLevel level;
    private final String deviceName;
    private final String message;

    LogItem(String level, String tag, String message) {
        this.level = LogLevel.fromString(level);
        this.deviceName = tag;
        this.message = message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMessage() {
        return message;
    }
}
