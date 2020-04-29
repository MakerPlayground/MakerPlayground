package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ContinuousAnimatedValue implements AnimatedValue {
    @Getter @Setter private CustomNumberExpression startValue;
    @Getter @Setter private CustomNumberExpression endValue;
    @Getter @Setter private CustomNumberExpression duration;
    @Getter @Setter private DelayUnit delayUnit;
    @Getter @Setter private Easing easing;

    public ContinuousAnimatedValue() {
        startValue = CustomNumberExpression.INVALID;
        endValue = CustomNumberExpression.INVALID;
        duration = CustomNumberExpression.INVALID;
        delayUnit = DelayUnit.SECOND;
        easing = new LinearEasing();
    }

    public static abstract class Easing {
        @Getter private final String name;

        Easing(String name) {
            this.name = name;
        }
    }

    public static class LinearEasing extends Easing {
        private static final LinearEasing instance = new LinearEasing();

        private LinearEasing() {
            super("Linear");
        }

        public static LinearEasing getInstance() {
            return instance;
        }
    }

    public static class BezierEasing extends Easing {
        @Getter private final double c1x;
        @Getter private final double c1y;
        @Getter private final double c2x;
        @Getter private final double c2y;

        private BezierEasing(double c1x, double c1y, double c2x, double c2y) {
            super("Bezier");
            this.c1x = c1x;
            this.c1y = c1y;
            this.c2x = c2x;
            this.c2y = c2y;
        }

        public static BezierEasing getInstance(double c1x, double c1y, double c2x, double c2y) {
            return new BezierEasing(c1x, c1y, c2x, c2y);
        }
    }
}
