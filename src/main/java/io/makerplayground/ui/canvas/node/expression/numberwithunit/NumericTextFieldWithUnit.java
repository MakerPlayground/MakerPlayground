package io.makerplayground.ui.canvas.node.expression.numberwithunit;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.ui.control.AutoResizeCombobox;
import io.makerplayground.ui.control.AutoResizeNumericTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;

import java.util.List;

public class NumericTextFieldWithUnit extends NumberWithUnitControl {

    private final ObjectProperty<NumberWithUnit> value;

    public NumericTextFieldWithUnit(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        value = new SimpleObjectProperty<>(initialValue);

        AutoResizeNumericTextField textField = new AutoResizeNumericTextField(min, max, initialValue.getValue());

        ComboBox<Unit> unitComboBox = new AutoResizeCombobox<>(FXCollections.observableArrayList(unit));
        unitComboBox.getSelectionModel().select(initialValue.getUnit());
        if (initialValue.getUnit() == Unit.NOT_SPECIFIED) {
            unitComboBox.setVisible(false);
            unitComboBox.setManaged(false);
        }

        value.addListener((observable, oldValue, newValue) -> {
            textField.setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });
        textField.valueProperty().addListener((observable, oldValue, newValue) -> {
            value.set(new NumberWithUnit(newValue.doubleValue(), value.get().getUnit()));
        });
        unitComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            value.set(new NumberWithUnit(value.get().getValue(), unitComboBox.getValue()));
        });

        setAlignment(Pos.BASELINE_LEFT);
        getChildren().addAll(textField, unitComboBox);
    }

    @Override
    public NumberWithUnit getValue() {
        return value.get();
    }

    @Override
    public void setValue(NumberWithUnit numberWithUnit) {
        value.set(numberWithUnit);
    }

    @Override
    public ObjectProperty<NumberWithUnit> valueProperty() {
        return value;
    }

}
