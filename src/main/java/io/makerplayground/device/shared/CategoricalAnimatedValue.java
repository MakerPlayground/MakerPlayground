package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.*;

import java.util.Collections;
import java.util.List;

public abstract class CategoricalAnimatedValue<T extends Expression> implements AnimatedValue {
    @Getter private final List<AnimatedKeyValue<T>> keyValues;

    public CategoricalAnimatedValue() {
        keyValues = Collections.emptyList();
    }

    public CategoricalAnimatedValue(List<AnimatedKeyValue<T>> keyValues) {
        this.keyValues = List.copyOf(keyValues);
    }

    public CategoricalAnimatedValue(CategoricalAnimatedValue<T> animatedValue) {
        keyValues = List.copyOf(animatedValue.keyValues);
    }

    @AllArgsConstructor
    public static class AnimatedKeyValue<T extends Expression> {
        @Getter @With private T value;
        @Getter @With private CustomNumberExpression delay;
        @Getter @With private DelayUnit delayUnit;
    }
}
