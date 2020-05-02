package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.*;

import java.util.List;

public abstract class CategoricalAnimatedValue<T extends Expression> implements AnimatedValue {
    @Getter private final ObservableList<AnimatedKeyValue<T>> keyValues;

    public CategoricalAnimatedValue() {
        keyValues = FXCollections.observableArrayList();
    }

    public CategoricalAnimatedValue(List<AnimatedKeyValue<T>> keyValues) {
        this.keyValues = FXCollections.observableList(keyValues);
    }

    public CategoricalAnimatedValue(CategoricalAnimatedValue<T> animatedValue) {
        this();
        keyValues.addAll(animatedValue.keyValues);
    }

    @AllArgsConstructor
    public static class AnimatedKeyValue<T extends Expression> {
        @Getter @With private T value;
        @Getter @With private CustomNumberExpression delay;
        @Getter @With private DelayUnit delayUnit;
    }
}
