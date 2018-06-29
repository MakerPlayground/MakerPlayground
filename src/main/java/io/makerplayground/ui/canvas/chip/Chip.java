package io.makerplayground.ui.canvas.chip;

import io.makerplayground.project.term.Term;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public abstract class Chip<T> extends StackPane {
    private Term.Type type;
    private ObjectProperty<T> value = new SimpleObjectProperty<>();

    public Chip(T initialValue, Term.Type type) {
        this.type = type;
        this.value.set(initialValue);
        initView();
        initEvent();
    }

    protected abstract void initView();

    protected void initEvent() {
        // allow the chip to be selected
    }

    public Term.Type getChipType() {
        return type;
    }

    public T getValue() {
        return value.get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T value) {
        this.value.set(value);
    }
}
