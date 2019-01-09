package io.makerplayground.ui.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class AutoResizeCombobox<T> extends ComboBox<T> {

    public AutoResizeCombobox() {
        this(FXCollections.emptyObservableList());
    }

    public AutoResizeCombobox(ObservableList<T> items) {
        super(items);
        valueProperty().addListener((observable, oldValue, newValue) -> adjustPreferredWidth());
        // when the value is changed before the control is drawn on the screen, previous adjustPreferredWidth() called
        // may not effective because the look up is failed so we force update the width when the control is first drawn
        // on the screen
        layoutBoundsProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                // remove this listener as adjustPreferredWidth() may change the layout bound and invoke this listener again
                layoutBoundsProperty().removeListener(this);
                adjustPreferredWidth();
            }
        });
        getStyleClass().add("auto-resize-combobox");
    }

    private void adjustPreferredWidth() {
        // text object use to display the current value of the combobox
        Text oldText = (Text) lookup(".list-cell > .text");
        // arrow button on the right side of the combobox
        StackPane arrowButton = (StackPane) lookup(".arrow-button");
        // lookup may failed when the value is updated before this control is drawn on the screen
        if (oldText == null || arrowButton == null) {
            return;
        }
        // Text object inside the combobox still contains the old value when the valueProperty() is changed (at least on
        // macOS) and there isn't any way to detect when it will be redrawn so we construct a new Text object with new
        // value using the font from the old Text object and measure it's size
        Text newText = new Text(getConverter().toString(getValue()));
        newText.setFont(oldText.getFont());
        setMaxWidth(newText.getBoundsInLocal().getWidth() + arrowButton.getBoundsInLocal().getWidth());
    }
}
