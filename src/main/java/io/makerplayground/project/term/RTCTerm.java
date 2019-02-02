package io.makerplayground.project.term;

import io.makerplayground.device.shared.RealTimeClock;

import java.time.LocalDateTime;

public class RTCTerm extends Term{

    public RTCTerm(RealTimeClock rtc) {
        super(Type.DATETIME, rtc);
    }

    @Override
    public RealTimeClock getValue() {
        return (RealTimeClock) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }
}
