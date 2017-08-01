package io.makerplayground.ui.canvas;

import com.sun.javafx.scene.control.skin.IntegerFieldSkin;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.converter.DoubleStringConverter;

import javax.naming.Binding;
import javax.script.Bindings;

/**
 * Created by USER on 12-Jul-17.
 */
public class SliderWithUnit extends HBox {
    private final Text text;
    private final Slider slider;
    private final ComboBox<Unit> comboBox;
    private final ObjectProperty<NumberWithUnit> numberWithUnit;
    private final ObservableList<Constraint> constraintList;

    public SliderWithUnit() {
        this(0, Unit.NOT_SPECIFIED);
    }

    public SliderWithUnit(double number , Unit unit) {
        text = new Text();
        slider = new Slider();
        comboBox = new ComboBox<>();
        constraintList = FXCollections.observableArrayList();
        numberWithUnit = new SimpleObjectProperty<>(new NumberWithUnit(number,unit));
        numberWithUnit.addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.getValue());
            comboBox.getSelectionModel().select(newValue.getUnit());
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(newValue.doubleValue(), numberWithUnit.get().getUnit()));
            text.textProperty().bind(slider.valueProperty().asString("%.2f"));
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

    public void setValue(NumberWithUnit numberWithUnit) {
        this.numberWithUnit.set(numberWithUnit);
    }

    public void setUnit(ObservableList<Unit> unitList) {
        comboBox.setItems(unitList);
    }

    public ObservableList<Constraint> getSliderConstraint() {
        return constraintList;
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
