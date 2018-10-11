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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Created by USER on 12-Jul-17.
 */
public class SpinnerWithUnit extends NumberWithUnitControl {
    private final Spinner<Double> spinner;
    private final ComboBox<Unit> unitComboBox;
    private final Text unitLabel; // TODO: Text is used here instead of Label as CSS from property window leak to their underlying control and mess up our layout
    private final ObjectProperty<NumberWithUnit> numberWithUnit;

    public SpinnerWithUnit(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max));
        spinner.setEditable(true);
        spinner.getValueFactory().setValue(initialValue.getValue());

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

        setSpacing(5);

        numberWithUnit = new SimpleObjectProperty<>(initialValue);
        numberWithUnit.addListener((observable, oldValue, newValue) -> {
            spinner.getValueFactory().setValue(newValue.getValue());
            unitComboBox.getSelectionModel().select(newValue.getUnit());
        });

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                numberWithUnit.set(new NumberWithUnit(oldValue, numberWithUnit.get().getUnit()));
            } else {
                numberWithUnit.set(new NumberWithUnit(newValue, numberWithUnit.get().getUnit()));
            }
        });
        unitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            numberWithUnit.set(new NumberWithUnit(numberWithUnit.get().getValue(), newValue));
        });
        getChildren().addAll(spinner, unitLabel, unitComboBox);
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
}
