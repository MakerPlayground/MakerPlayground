package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@AllArgsConstructor
public class ContinuousAnimatedValue implements AnimatedValue {
    @Getter @With private CustomNumberExpression startValue;
    @Getter @With private CustomNumberExpression endValue;
    @Getter @With private CustomNumberExpression duration;
    @Getter @With private DelayUnit delayUnit;
    @Getter @With private Easing easing;

    public ContinuousAnimatedValue() {
        startValue = CustomNumberExpression.INVALID;
        endValue = CustomNumberExpression.INVALID;
        duration = CustomNumberExpression.INVALID;
        delayUnit = DelayUnit.SECOND;
        easing = new LinearEasing();
    }

    public static abstract class Easing {
        public abstract String getName();
    }

    public static abstract class BezierEasing extends Easing {
        @Getter private final double c1x;
        @Getter private final double c1y;
        @Getter private final double c2x;
        @Getter private final double c2y;

        public BezierEasing(double c1x, double c1y, double c2x, double c2y) {
            this.c1x = c1x;
            this.c1y = c1y;
            this.c2x = c2x;
            this.c2y = c2y;
        }
    }

    public static class LinearEasing extends BezierEasing {
        private static final LinearEasing instance = new LinearEasing();

        private LinearEasing() {
            super(0, 0, 1, 1);
        }

        public static LinearEasing getInstance() {
            return instance;
        }

        @Override
        public String getName() {
            return "Linear";
        }
    }

    public static class EaseInExpo extends BezierEasing {
        private static final EaseInExpo instance = new EaseInExpo();

        private EaseInExpo() {
            super(0.7, 0, 0.84, 0);
        }

        public static EaseInExpo getInstance() {
            return instance;
        }

        @Override
        public String getName() {
            return "EaseInExpo";
        }
    }

    public static class CustomEasing extends BezierEasing {
        private static final CustomEasing instance = new CustomEasing(0, 0, 1, 1);

        public CustomEasing(double c1x, double c1y, double c2x, double c2y) {
            super(c1x, c1y, c2x, c2y);
        }

        public static CustomEasing getInstance() {
            return instance;
        }

        @Override
        public String getName() {
            return "Custom";
        }
    }
}
