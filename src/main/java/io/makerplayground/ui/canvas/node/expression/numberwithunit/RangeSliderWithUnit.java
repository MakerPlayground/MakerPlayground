/*
 * Copyright (c) 2018. The Maker Playground Authors.
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.controlsfx.control.RangeSlider;

import java.text.DecimalFormat;
import java.util.List;

public class RangeSliderWithUnit extends HBox {
    private final RangeSlider slider;
    private final Spinner<Double> lowSpinner;
    private final Spinner<Double> highSpinner;
    private final ComboBox<Unit> unitComboBox;
    private final Text unitLabel;  // TODO: Text is used here instead of Label as CSS from property window leak to their underlying control and mess up our layout

    private final ObjectProperty<NumberWithUnit> minValue;
    private final ObjectProperty<NumberWithUnit> maxValue;
    private final ObjectProperty<NumberWithUnit> lowValue;
    private final ObjectProperty<NumberWithUnit> highValue;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public RangeSliderWithUnit() {
        this(0, 0, List.of(Unit.NOT_SPECIFIED), NumberWithUnit.ZERO);
    }

    public RangeSliderWithUnit(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        slider = new RangeSlider();
        slider.setMax(max);
        slider.setMin(min);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        lowSpinner = new Spinner<>(min, max, initialValue.getValue());
        lowSpinner.setEditable(true);

        highSpinner = new Spinner<>(min, max, initialValue.getValue());
        highSpinner.setEditable(true);

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

        lowValue = new SimpleObjectProperty<>(initialValue);
        highValue = new SimpleObjectProperty<>(initialValue);
        lowValue.addListener((observable, oldValue, newValue) -> {
            slider.setHighValue(highValue.get().getValue());
            slider.setLowValue(newValue.getValue());
            slider.setHighValue(highValue.get().getValue());
            lowSpinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });
        highValue.addListener((observable, oldValue, newValue) -> {
            slider.setHighValue(newValue.getValue());
            slider.setLowValue(lowValue.get().getValue());
            slider.setHighValue(newValue.getValue());
            highSpinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });

        minValue = new SimpleObjectProperty<>(new NumberWithUnit(min, unit.get(0)));
        maxValue = new SimpleObjectProperty<>(new NumberWithUnit(max, unit.get(0)));
        minValue.addListener((observable, oldValue, newValue) -> {
            slider.setMax(maxValue.get().getValue());
            slider.setMin(newValue.getValue());
            lowSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(newValue.getValue()
                    , maxValue.get().getValue()));
            lowSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                lowValue.set(new NumberWithUnit(newValue1, getLowValue().getUnit()));
            });
            highSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(newValue.getValue()
                    , maxValue.get().getValue()));
            highSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                highValue.set(new NumberWithUnit(newValue1, getHighValue().getUnit()));
            });
        });
        maxValue.addListener((observable, oldValue, newValue) -> {
            slider.setMax(newValue.getValue());
            slider.setMin(minValue.get().getValue());
            lowSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue.get().getValue()
                    , newValue.getValue()));
            lowSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                lowValue.set(new NumberWithUnit(newValue1, getLowValue().getUnit()));
            });
            highSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue.get().getValue()
                    , newValue.getValue()));
            highSpinner.getValueFactory().valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                highValue.set(new NumberWithUnit(newValue1, getHighValue().getUnit()));
            });
        });

        slider.lowValueProperty().addListener(((observable, oldValue, newValue) -> {
            lowValue.set(new NumberWithUnit(newValue.doubleValue(), lowValue.get().getUnit()));
        }));
        slider.highValueProperty().addListener(((observable, oldValue, newValue) -> {
            highValue.set(new NumberWithUnit(newValue.doubleValue(), highValue.get().getUnit()));
        }));

        lowSpinner.getValueFactory().valueProperty().addListener(((observable, oldValue, newValue) -> {
            lowValue.set(new NumberWithUnit(newValue, lowValue.get().getUnit()));
        }));
        highSpinner.getValueFactory().valueProperty().addListener(((observable, oldValue, newValue) -> {
            highValue.set(new NumberWithUnit(newValue, highValue.get().getUnit()));
        }));

        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            highValue.set(new NumberWithUnit(highValue.get().getValue(), newValue));
            lowValue.set(new NumberWithUnit(lowValue.get().getValue(), newValue));
        });

        setSpacing(5);
        setAlignment(Pos.CENTER);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        getChildren().addAll(lowSpinner, slider, highSpinner, unitLabel, unitComboBox);
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

    public ObjectProperty<NumberWithUnit> lowValueProperty() {
        return lowValue;
    }

    public void setLowValue(NumberWithUnit lowValue) {
        this.lowValue.set(lowValue);
    }

    public NumberWithUnit getHighValue() {
        return highValue.get();
    }

    public ObjectProperty<NumberWithUnit> highValueProperty() {
        return highValue;
    }

    public void setHighValue(NumberWithUnit highValue) {
        this.highValue.set(highValue);
    }
}
