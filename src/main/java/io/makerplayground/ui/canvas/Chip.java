package io.makerplayground.ui.canvas;

import io.makerplayground.project.chip.ChipType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public abstract class Chip<T> extends StackPane implements Selectable {
    private ChipType type;
    private ObjectProperty<T> value = new SimpleObjectProperty<>();
    private final BooleanProperty select = new SimpleBooleanProperty(false);

    public Chip(T initialValue, ChipType type) {
        this.type = type;
        this.value.set(initialValue);
        initView();
        initEvent();
    }

    protected abstract void initView();

    protected void initEvent() {
        // allow the chip to be selected
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> select.set(true));
    }

    public ChipType getChipType() {
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

    @Override
    public BooleanProperty selectedProperty() {
        return select;
    }

    @Override
    public boolean isSelected() {
        return select.get();
    }

    @Override
    public void setSelected(boolean b) {
        select.set(b);
    }
}
