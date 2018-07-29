package io.makerplayground.ui.canvas.node.usersetting.expression;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.ui.canvas.node.expressioncontrol.NumberWithUnitControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Created by USER on 12-Jul-17.
 */
public class SliderWithUnit extends NumberWithUnitControl {
    private final Slider slider;
    private final TextField textField;
    private final ComboBox<Unit> unitComboBox;
    private final Text unitLabel;  // TODO: Text is used here instead of Label as CSS from property window leak to their underlying control and mess up our layout
    private final ObjectProperty<NumberWithUnit> numberWithUnit;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final UnaryOperator<TextFormatter.Change> textFilter = t -> {
        if (t.isReplaced()) {
            if (t.getText().matches("[^0-9]")) {
                t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));
            }
        }

        if (t.isAdded()) {
            if (t.getControlText().contains(".")) {
                if (t.getText().matches("[^0-9]")) {
                    t.setText("");
                }
            } else if (t.getText().matches("[^0-9.]")) {
                t.setText("");
            }
        }

        return t;
    };

    public SliderWithUnit(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        slider = new Slider(min, max, initialValue.getValue());

        textField = new TextField(decimalFormat.format(initialValue.getValue()));
        textField.setTextFormatter(new TextFormatter<Double>(textFilter));

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
            textField.setText(decimalFormat.format(newValue.getValue()));
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(newValue.doubleValue(), numberWithUnit.get().getUnit()));
        });
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                numberWithUnit.set(new NumberWithUnit(0, numberWithUnit.get().getUnit()));
            } else {
                numberWithUnit.set(new NumberWithUnit(Double.parseDouble(newValue), numberWithUnit.get().getUnit()));
            }
        });
        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(numberWithUnit.get().getValue(), newValue));
        });

        setSpacing(5);
        setAlignment(Pos.CENTER);
        getChildren().addAll(slider, textField, unitLabel, unitComboBox);
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
