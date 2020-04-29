package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;

import java.util.List;

public class NumericCategoricalAnimatedValue extends CategoricalAnimatedValue<CustomNumberExpression> {
    public NumericCategoricalAnimatedValue() {
    }

    public NumericCategoricalAnimatedValue(List<AnimatedKeyValue<CustomNumberExpression>> keyValue) {
        super(keyValue);
    }
}
