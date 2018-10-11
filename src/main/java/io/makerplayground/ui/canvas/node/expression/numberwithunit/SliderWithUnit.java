package io.makerplayground.ui.canvas.node.expression.numberwithunit;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by USER on 12-Jul-17.
 */
public class SliderWithUnit extends NumberWithUnitControl {
    private final Slider slider;
    private final Spinner<Number> spinner;
    private final ComboBox<Unit> unitComboBox;
    private final Text unitLabel;  // TODO: Text is used here instead of Label as CSS from property window leak to their underlying control and mess up our layout
    private final ObjectProperty<NumberWithUnit> numberWithUnit;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public SliderWithUnit(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        slider = new Slider(min, max, initialValue.getValue());

        spinner = new Spinner<>(min, max, initialValue.getValue());
        spinner.setEditable(true);
        spinner.getValueFactory().valueProperty().bindBidirectional(slider.valueProperty());

        unitLabel = new Text(initialValue.getUnit().toString());
        unitComboBox = new ComboBox<>(FXCollections.observableArrayList(unit));
        unitComboBox.getSelectionModel().select(initialValue.getUnit());
        if (unit.size() == 1 && initialValue.getUnit() == Unit.NOT_SPECIFIED) {
            unitComboBox.setVisible(false);
            unitComboBox.setManaged(false);
            unitLabel.setVisible(false);
            unitLabel.setManaged(false);
        } else if (unit.size() == 1) {
            unitComboBox.setVisible(false);
            unitComboBox.setManaged(false);
        } else {    // unit.size() > 1
            unitLabel.setVisible(false);
            unitLabel.setManaged(false);
        }

        numberWithUnit = new SimpleObjectProperty<>(initialValue);
        numberWithUnit.addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.getValue());
            spinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(newValue.doubleValue(), numberWithUnit.get().getUnit()));
        });
        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(numberWithUnit.get().getValue(), newValue));
        });

        setSpacing(5);
        setAlignment(Pos.CENTER);
        getChildren().addAll(slider, spinner, unitLabel, unitComboBox);
    }

    @Override
    public NumberWithUnit getValue() {
        return numberWithUnit.get();
    }

    @Override
    public ObjectProperty<NumberWithUnit> valueProperty() {
        return numberWithUnit;
    }

    @Override
    public void setValue(NumberWithUnit numberWithUnit) {
        this.numberWithUnit.set(numberWithUnit);
    }
}
