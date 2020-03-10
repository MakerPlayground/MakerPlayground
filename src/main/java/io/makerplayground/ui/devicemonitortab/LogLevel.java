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

enum LogLevel {
    INFO("[[I]]", 0),
    VALUE("[[V]]", 1),
    ERROR("[[E]]", 2);

    String levelTag;
    int priority;

    LogLevel(String levelTag, int priority) {
        this.levelTag = levelTag;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static LogLevel fromString(String levelTag) {
        for (LogLevel level: LogLevel.values()) {
            if (level.levelTag.equals(levelTag.trim())) {
                return level;
            }
        }
        throw new IllegalStateException("Cannot find LogLevel of tag: " + levelTag);
    }
}
