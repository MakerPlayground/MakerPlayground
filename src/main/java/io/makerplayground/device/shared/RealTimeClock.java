package io.makerplayground.device.shared;

import java.time.LocalDateTime;

public class RealTimeClock {

    public enum Mode {
        // TODO: Support mode NOW
        SPECIFIC
    }

    private final Mode mode;
    private final LocalDateTime localDateTime;

    public RealTimeClock(Mode mode, LocalDateTime localDateTime) {
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
