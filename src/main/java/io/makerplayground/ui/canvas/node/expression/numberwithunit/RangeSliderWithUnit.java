/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas.node.expression.numberwithunit;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;
import org.controlsfx.control.RangeSlider;

import java.util.List;

public class RangeSliderWithUnit extends HBox {
    private final RangeSlider slider;
    private final Spinner<Double> lowSpinner;
    private final Spinner<Double> highSpinner;
    private final ComboBox<Unit> unitComboBox;
    private final Text unitLabel;  // TODO: Text is used here instead of Label as CSS from property window leak to their underlying control and mess up our layout
    private Pane rangeBar;  // the area between the two thumbs of the RangeSlider which we will need to change its color when the RangeSlider is inverse

    private final ObjectProperty<NumberWithUnit> minValue;
    private final ObjectProperty<NumberWithUnit> maxValue;
    private final ReadOnlyObjectWrapper<NumberWithUnit> lowValue;  // begin value of the range selected which may be greater than the highValue when the RangeSlider is inverse
    private final ReadOnlyObjectWrapper<NumberWithUnit> highValue; // end value of the range selected which may be less than the lowValue when the RangeSlider is inverse
    private final BooleanProperty inverse;

    private static final String inverseRangeBarStyle = "-fx-background-color: red";
    private static final NumberStringConverter defaultNumberStringConverter = new NumberStringConverter();
    // the RangeSlide doesn't have an inverse property so we inverse the tick label instead
    private final NumberStringConverter inverseNumberStringConverter = new NumberStringConverter() {
        @Override
        public Number fromString(String value) {
            return inverse.get() ? inverseNumber(super.fromString(value)) : super.fromString(value);
        }

        @Override
        public String toString(Number value) {
            return inverse.get() ? super.toString(inverseNumber(value)) : super.toString(value);
        }
    };

    public RangeSliderWithUnit() {
        this(0, 0, NumberWithUnit.ZERO, NumberWithUnit.ZERO, false, List.of(Unit.NOT_SPECIFIED));
    }

    public RangeSliderWithUnit(double min, double max, NumberWithUnit low, NumberWithUnit high, boolean inv, List<Unit> unit) {
        minValue = new SimpleObjectProperty<>(new NumberWithUnit(min, unit.get(0)));
        maxValue = new SimpleObjectProperty<>(new NumberWithUnit(max, unit.get(0)));
        lowValue = new ReadOnlyObjectWrapper<>(low);
        highValue = new ReadOnlyObjectWrapper<>(high);

        slider = new RangeSlider();
        slider.setMax(max);
        slider.setMin(min);
        // we don't actually inverse the RangeSlider so the value set to the slider must be uninverse
        if (inv) {
            slider.setHighValue(unInverseNumberWithUnit(high).getValue());
            slider.setLowValue(unInverseNumberWithUnit(low).getValue());
            slider.setHighValue(unInverseNumberWithUnit(high).getValue());
        } else {
            slider.setHighValue(high.getValue());
            slider.setLowValue(low.getValue());
            slider.setHighValue(high.getValue());
        }
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        lowSpinner = new Spinner<>(min, max, low.getValue());
        lowSpinner.setEditable(true);

        highSpinner = new Spinner<>(min, max, high.getValue());
        highSpinner.setEditable(true);

        unitLabel = new Text(low.getUnit().toString());     // assume that low and high use the same unit
        unitComboBox = new ComboBox<>(FXCollections.observableArrayList(unit));
        unitComboBox.getSelectionModel().select(low.getUnit());
        if (unit.size() == 1 && low.getUnit() == Unit.NOT_SPECIFIED) {
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

        inverse = new SimpleBooleanProperty(inv);
        if (inv) {
            slider.setLabelFormatter(inverseNumberStringConverter);
            if (rangeBar != null) {
                rangeBar.setStyle(inverseRangeBarStyle);
            }
        } else {
            slider.setLabelFormatter(defaultNumberStringConverter);
            if (rangeBar != null) {
                rangeBar.setStyle("");
            }
        }
        inverse.addListener((observable, oldValue, newValue) -> {
            NumberWithUnit lv = lowValue.get();
            NumberWithUnit hv = highValue.get();
            lowValue.set(hv);
            highValue.set(lv);
            if (newValue) {
                slider.setLabelFormatter(inverseNumberStringConverter);
                if (rangeBar != null) {
                    rangeBar.setStyle(inverseRangeBarStyle);
                }
            } else {
                slider.setLabelFormatter(defaultNumberStringConverter);
                if (rangeBar != null) {
                    rangeBar.setStyle("");
                }
            }
        });
        // we don't actually inverse the RangeSlider so the value set to the slider must be uninverse
        lowValue.addListener((observable, oldValue, newValue) -> {
            double sliderLowValue = inverse.get() ? unInverseNumberWithUnit(newValue).getValue() : newValue.getValue();
            double sliderHighValue = inverse.get() ? unInverseNumberWithUnit(highValue.get()).getValue() : highValue.get().getValue();
            slider.setHighValue(sliderHighValue);
            slider.setLowValue(sliderLowValue);
            slider.setHighValue(sliderHighValue);
            lowSpinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });
        highValue.addListener((observable, oldValue, newValue) -> {
            double sliderLowValue = inverse.get() ? unInverseNumberWithUnit(lowValue.get()).getValue() : lowValue.get().getValue();
            double sliderHighValue = inverse.get() ? unInverseNumberWithUnit(newValue).getValue() : newValue.getValue();
            slider.setHighValue(sliderHighValue);
            slider.setLowValue(sliderLowValue);
            slider.setHighValue(sliderHighValue);
            highSpinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });

        minValue.addListener((observable, oldValue, newValue) -> {
            slider.setMax(maxValue.get().getValue());
            slider.setMin(newValue.getValue());
            initializeSpinner();
        });
        maxValue.addListener((observable, oldValue, newValue) -> {
            slider.setMax(newValue.getValue());
            slider.setMin(minValue.get().getValue());
            initializeSpinner();
        });

        // save the actual value (inverse of the slider value when the inverse property is true) to the low/highValue property when the slider's thumb is moved
        slider.lowValueProperty().addListener(((observable, oldValue, newValue) -> {
            if (inverse.get()) {
                lowValue.set(inverseNumberWithUnit(new NumberWithUnit(newValue.doubleValue(), lowValue.get().getUnit())));
            } else {
                lowValue.set(new NumberWithUnit(newValue.doubleValue(), lowValue.get().getUnit()));
            }
        }));
        slider.highValueProperty().addListener(((observable, oldValue, newValue) -> {
            if (inverse.get()) {
                highValue.set(inverseNumberWithUnit(new NumberWithUnit(newValue.doubleValue(), highValue.get().getUnit())));
            } else {
                highValue.set(new NumberWithUnit(newValue.doubleValue(), highValue.get().getUnit()));
            }
        }));

        initializeSpinner();

        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            highValue.set(new NumberWithUnit(highValue.get().getValue(), newValue));
            lowValue.set(new NumberWithUnit(lowValue.get().getValue(), newValue));
        });

        setSpacing(5);
        setAlignment(Pos.CENTER);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        getChildren().addAll(lowSpinner, slider, highSpinner, unitLabel, unitComboBox);

        // get the instance of the rangeBar after the slider is drawn on the screen by detecting when the layoutBound changed
        layoutBoundsProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                rangeBar = (Pane) lookup(".range-slider .range-bar");
                if (inverse.get()) {
                    rangeBar.setStyle(inverseRangeBarStyle);
                }
                layoutBoundsProperty().removeListener(this);
            }
        });
    }

    private void initializeSpinner() {
        lowSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue.get().getValue(), maxValue.get().getValue(), lowValue.get().getValue()));
        lowSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((inverse.get() && (newValue1 < highValue.get().getValue())) || (!inverse.get() && (newValue1 > highValue.get().getValue()))) {
                lowSpinner.getValueFactory().setValue(highValue.get().getValue());
            } else {
                // the spinner always contains the actual value so we don't need to perform any inversion
                lowValue.set(new NumberWithUnit(newValue1, lowValue.get().getUnit()));
            }
        });
        highSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue.get().getValue(), maxValue.get().getValue(), highValue.get().getValue()));
        highSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
            if ((inverse.get() && (newValue1 > lowValue.get().getValue())) || (!inverse.get() && (newValue1 < lowValue.get().getValue()))) {
                highSpinner.getValueFactory().setValue(lowValue.get().getValue());
            } else {
                highValue.set(new NumberWithUnit(newValue1, highValue.get().getUnit()));
            }
        });
    }

    private Number inverseNumber(Number value) {
        double min = minValue.get().getValue();
        double max = maxValue.get().getValue();
        return (value.doubleValue() - min) * (min - max) / (max - min) + max;   // (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private Number unInverseNumber(Number value) {
        double min = minValue.get().getValue();
        double max = maxValue.get().getValue();
        return (value.doubleValue() - max) * (max - min) / (min - max) + min;
    }

    private NumberWithUnit inverseNumberWithUnit(NumberWithUnit value) {
        return new NumberWithUnit(inverseNumber(value.getValue()).doubleValue(), value.getUnit());
    }

    private NumberWithUnit unInverseNumberWithUnit(NumberWithUnit value) {
        return new NumberWithUnit(unInverseNumber(value.getValue()).doubleValue(), value.getUnit());
    }

    public NumberWithUnit getMinValue() {
        return minValue.get();
    }

    public ObjectProperty<NumberWithUnit> minValueProperty() {
        return minValue;
    }

    public void setMinValue(NumberWithUnit minValue) {
        this.minValue.set(minValue);
    }

    public NumberWithUnit getMaxValue() {
        return maxValue.get();
    }

    public ObjectProperty<NumberWithUnit> maxValueProperty() {
        return maxValue;
    }

    public void setMaxValue(NumberWithUnit maxValue) {
        this.maxValue.set(maxValue);
    }

    public NumberWithUnit getLowValue() {
        return lowValue.get();
    }

    public ReadOnlyObjectProperty<NumberWithUnit> lowValueProperty() {
        return lowValue.getReadOnlyProperty();
    }

    public void setLowValue(NumberWithUnit lowValue) {
        this.lowValue.set(lowValue);
    }

    public NumberWithUnit getHighValue() {
        return highValue.get();
    }

    public ReadOnlyObjectProperty<NumberWithUnit> highValueProperty() {
        return highValue.getReadOnlyProperty();
    }

    public void setHighValue(NumberWithUnit highValue) {
        this.highValue.set(highValue);
    }

    public boolean isInverse() {
        return inverse.get();
    }

    public BooleanProperty inverseProperty() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse.set(inverse);
    }
}
