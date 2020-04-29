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

    @AllArgsConstructor
    public static class AnimatedKeyValue<T extends Expression> {
        @Getter @Setter private T value;
        @Getter @Setter private CustomNumberExpression delay;
        @Getter @Setter private DelayUnit delayUnit;
    }
}
