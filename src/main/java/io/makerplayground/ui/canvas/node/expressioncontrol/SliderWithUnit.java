package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * Created by USER on 12-Jul-17.
 */
public class SliderWithUnit extends HBox {
    private final Text text;
    private final Slider slider;
    private final ComboBox<Unit> comboBox;
    private final ObjectProperty<NumberWithUnit> numberWithUnit;

    public SliderWithUnit(double min, double max, ObservableList<Unit> unit, NumberWithUnit initialValue) {
        slider = new Slider(min, max, initialValue.getValue());
        text = new Text();
        text.textProperty().bind(slider.valueProperty().asString("%.2f"));
        comboBox = new ComboBox<>(unit);
        comboBox.getSelectionModel().select(initialValue.getUnit());
        if (initialValue.getUnit() == Unit.NOT_SPECIFIED) {
            comboBox.setVisible(false);
            comboBox.setManaged(false);
        }
        numberWithUnit = new SimpleObjectProperty<>(new NumberWithUnit(initialValue.getValue(), initialValue.getUnit()));
        numberWithUnit.addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.getValue());
            comboBox.getSelectionModel().select(newValue.getUnit());
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(newValue.doubleValue(), numberWithUnit.get().getUnit()));
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(numberWithUnit.get().getValue(), newValue));
        });
        setSpacing(2);
        setAlignment(Pos.CENTER);
        getChildren().addAll(slider,text, comboBox);
    }

    public NumberWithUnit getValue() {
        return numberWithUnit.get();
    }

    public ObjectProperty<NumberWithUnit> valueProperty() {
        return numberWithUnit;
    }

    public static class Constraint {
        private final double min;
        private final double max;
        private final Unit unit;

        public Constraint(double min, double max, Unit unit) {
            this.min = min;
            this.max = max;
            this.unit = unit;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public Unit getUnit() {
            return unit;
        }
    }
}
