package io.makerplayground.device.shared;

import io.makerplayground.project.expression.ComplexStringExpression;

import java.util.List;

public class StringCategoricalAnimatedValue extends CategoricalAnimatedValue<ComplexStringExpression> {
    public StringCategoricalAnimatedValue() {
    }

    public StringCategoricalAnimatedValue(List<AnimatedKeyValue<ComplexStringExpression>> keyValue) {
        super(keyValue);
    }
}
