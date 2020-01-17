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

package io.makerplayground.device.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RealTimeClock {

    public enum Mode {
        // TODO: Support mode NOW
        SPECIFIC
    }

    private final Mode mode;
    private final LocalDateTime localDateTime;

    @JsonCreator
    public RealTimeClock(@JsonProperty("mode") Mode mode, @JsonProperty("localDateTime") LocalDateTime localDateTime) {
        this.mode = mode;
        this.localDateTime = localDateTime;
    }

    public RealTimeClock(RealTimeClock rtc) {
        this(rtc.mode, rtc.localDateTime);
    }

    public static RealTimeClock getDefault() {
        // TODO: Support mode NOW
        return new RealTimeClock(Mode.SPECIFIC, LocalDateTime.now().plusMinutes(2));
    }

    public Mode getMode() {
        return mode;
    }

    public LocalDateTime getLocalDateTime() {
        // TODO: Support mode NOW
//        return mode == Mode.NOW ? LocalDateTime.now() : localDateTime;
        return localDateTime;
    }
}
