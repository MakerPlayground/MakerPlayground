package io.makerplayground.project.expression;

import io.makerplayground.device.shared.RealTimeClock;
import io.makerplayground.project.term.RTCTerm;

import java.time.LocalDateTime;

public class SimpleRTCExpression extends Expression {

    public SimpleRTCExpression(RealTimeClock rtc) {
        super(Type.DATETIME);
        terms.add(new RTCTerm(rtc));
    }

    public RealTimeClock getRealTimeClock() {
        return (RealTimeClock) getTerms().get(0).getValue();
    }

    public SimpleRTCExpression(SimpleRTCExpression rtcExpression) {
        super(rtcExpression);
    }

    @Override
    public SimpleRTCExpression deepCopy() {
        return new SimpleRTCExpression(this);
    }


}
