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
