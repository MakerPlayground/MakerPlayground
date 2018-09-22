package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.term.Term;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.StackPane;

import java.util.Collections;
import java.util.List;

public abstract class Chip<T> extends StackPane {
    private final Term.Type type;
    private final ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper<>();
    private final List<T> choices;

    public Chip(T initialValue, Term.Type type) {
        this(initialValue, type, Collections.emptyList());
    }

    public Chip(T initialValue, Term.Type type, List<T> choices) {
        this.type = type;
        this.value.set(initialValue);
        this.choices = choices;

        initView();
        // use geometric shape of this node instead of the bounding box for mouse event
        setPickOnBounds(false);
    }

    protected abstract void initView();

    public abstract Term getTerm();

    public Term.Type getChipType() {
        return type;
    }

    public T getValue() {
        return value.get();
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return value.getReadOnlyProperty();
    }

    protected void setValue(T value) {
        this.value.set(value);
    }

    public List<T> getChoices() {
        return choices;
    }
}
