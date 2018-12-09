package io.makerplayground.project.term;

import io.makerplayground.device.shared.RealTimeClock;

import java.time.LocalDateTime;

public class RTCTerm extends Term{

    public RTCTerm(RealTimeClock rtc) {
        super(Type.DATETIME, rtc);
    }

    @Override
    public String toCCode() {
        LocalDateTime rtc = getValue().getLocalDateTime();
        return "MP_DATETIME(" + rtc.getSecond() + "," + rtc.getMinute() + "," + rtc.getHour() +  "," + rtc.getDayOfMonth() + "," + rtc.getMonth().getValue() + "," + rtc.getYear() + ")";
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
