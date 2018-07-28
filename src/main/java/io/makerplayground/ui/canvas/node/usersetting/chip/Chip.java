package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Term;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import java.util.List;

public abstract class Chip<T> extends StackPane {
    private Term.Type type;
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    private List<T> choices;

    public Chip(T initialValue, Term.Type type) {
        this(initialValue, type, null);
    }

    public Chip(T initialValue, Term.Type type, List<T> choices) {
        this.type = type;
        this.value.set(initialValue);
        this.choices = choices;
        initView();
        initEvent();
    }

    List<T> getChoices() {
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
