package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Term;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

public abstract class Chip<T> extends StackPane {
    private Term.Type type;
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    private ListProperty<T> choices = new SimpleListProperty<>();

    public Chip(T initialValue, Term.Type type) {
        this(initialValue, type, null);
    }

    public Chip(T initialValue, Term.Type type, ObservableList<T> choices) {
        this.type = type;
        this.value.set(initialValue);
        this.choices.setValue(choices);
        initView();
        initEvent();
    }

    ObservableList<T> getChoices() {
        return choices.get();
    }

    public ListProperty<T> choicesProperty() {
        return choices;
    }

    protected abstract void initView();

    public abstract Term getTerm();

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
