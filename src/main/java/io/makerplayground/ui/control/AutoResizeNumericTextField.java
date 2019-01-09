package io.makerplayground.ui.control;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class AutoResizeNumericTextField extends AutoResizeTextField {

    private final DoubleProperty value;

    public AutoResizeNumericTextField() {
        this(Double.MIN_VALUE, Double.MAX_VALUE, 0);
    }

    public AutoResizeNumericTextField(double min, double max, double initialValue) {
        super();
        setText(String.valueOf(initialValue));

        value = new SimpleDoubleProperty(initialValue);
        value.addListener((observable, oldValue, newValue) -> {
            if (!getText().isEmpty() && Double.compare(Double.parseDouble(getText()), newValue.doubleValue()) != 0) {
                setText(String.valueOf(newValue));
            }
        });

        textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                value.setValue(0);
            } else if (newValue.matches("-?\\d+\\.?\\d*")) {
                double newDoubleValue = Double.parseDouble(newValue);
                if (Double.compare(newDoubleValue, value.get()) != 0) {
                    if (newDoubleValue < min) {
                        value.setValue(min);
                    } else if (newDoubleValue > max) {
                        value.setValue(max);
                    } else {
                        value.setValue(newDoubleValue);
                    }
                }
            } else {
                setText(oldValue);
            }
        });
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (getText().isEmpty()) {
                setText("0");
            }
        });

        getStyleClass().add("auto-resize-numeric-text-field");
    }

    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setValue(double value) {
        this.value.set(value);
    }
}
