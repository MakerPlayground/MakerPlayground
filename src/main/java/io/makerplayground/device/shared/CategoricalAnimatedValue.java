package io.makerplayground.device.shared;

import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.*;

public abstract class CategoricalAnimatedValue<T extends Expression> implements AnimatedValue {
    @Getter private final ObservableList<AnimatedKeyValue<T>> keyValues;

    public CategoricalAnimatedValue() {
        keyValues = FXCollections.observableArrayList();
    }

    @AllArgsConstructor
    public static class AnimatedKeyValue<T extends Expression> {
        @Getter @Setter private T value;
        @Getter @Setter private CustomNumberExpression delay;
        @Getter @Setter private DelayUnit delayUnit;
    }
}
