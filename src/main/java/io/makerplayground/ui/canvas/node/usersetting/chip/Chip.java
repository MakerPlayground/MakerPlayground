package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Term;
import io.makerplayground.ui.canvas.node.Selectable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.util.Collections;
import java.util.List;

public abstract class Chip<T> extends StackPane implements Selectable {
    private final Term.Type type;
    private final ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper<>();
    private final List<T> choices;

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Chip(T initialValue, Term.Type type) {
        this(initialValue, type, Collections.emptyList());
    }

    public Chip(T initialValue, Term.Type type, List<T> choices) {
        this.type = type;
        this.value.set(initialValue);
        this.choices = choices;

        initView();
        updateChipStyle(false);
        // use geometric shape of this node instead of the bounding box for mouse event
        setPickOnBounds(false);
        selected.addListener((observable, oldValue, newValue) -> {
            updateChipStyle(newValue);
            requestFocus();
        });

        // check for chip selection in an event filter to prevent children node of chip e.g. TextField, Combobox etc.
        // to consume mouse event and prevent the chip from being selected
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> selected.set(true));
        // consume mouse event in an event handler to differentiate between pressing on the chip and pressing on
        // the empty area of the chip field
        addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);
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

    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    @Override
    public void setSelected(boolean b) {
        selected.set(b);
    }

    protected abstract void updateChipStyle(boolean selected);

}
