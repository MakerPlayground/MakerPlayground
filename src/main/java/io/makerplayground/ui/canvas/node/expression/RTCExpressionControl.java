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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.RealTimeClock;
import io.makerplayground.project.expression.SimpleRTCExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RTCExpressionControl extends HBox {
    private static final StringConverter<Integer> twoDigitStringConverter = new StringConverter<>() {
        DecimalFormat df = new DecimalFormat("00");
        @Override
        public String toString(Integer object) {
            return df.format(object);
        }
        @Override
        public Integer fromString(String string) {
            try {
                return df.parse(string).intValue();
            } catch (ParseException e) {
                return 0;
            }
        }
    };

    private final ReadOnlyObjectWrapper<SimpleRTCExpression> expression;

    public RTCExpressionControl(SimpleRTCExpression initialValue) {
        expression = new ReadOnlyObjectWrapper<>(initialValue);

        LocalDateTime datetime = LocalDateTime.now();
        if (expression.get().getTerms().size() > 0) {
            if (expression.get().getRealTimeClock().getMode() == RealTimeClock.Mode.SPECIFIC) {
                datetime = expression.get().getRealTimeClock().getLocalDateTime();
            } else {
                // TODO: Support mode NOW
                System.err.println("No implementation for NOW mode right now.");
            }
        }
        DatePicker datePicker = new DatePicker(datetime.toLocalDate());

        SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23);
        hourValueFactory.setConverter(twoDigitStringConverter);
        hourValueFactory.setValue(datetime.getHour());
        Spinner<Integer> hourSpinner = new Spinner<>(hourValueFactory);
        hourSpinner.setId("timeSpinner");
        hourSpinner.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[0-9]+")) {
                hourSpinner.getEditor().setText("0");
            }
        }));
        hourSpinner.setEditable(true);

        SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
        minuteValueFactory.setConverter(twoDigitStringConverter);
        minuteValueFactory.setValue(datetime.getMinute());
        Spinner<Integer> minuteSpinner = new Spinner<>(minuteValueFactory);
        minuteSpinner.setId("timeSpinner");
        minuteSpinner.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[0-9]+")) {
                minuteSpinner.getEditor().setText("0");
            }
        }));
        minuteSpinner.setEditable(true);

        SpinnerValueFactory<Integer> secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
        secondValueFactory.setConverter(twoDigitStringConverter);
        secondValueFactory.setValue(datetime.getSecond());
        Spinner<Integer> secondSpinner = new Spinner<>(secondValueFactory);
        secondSpinner.setId("timeSpinner");
        secondSpinner.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[0-9]+")) {
                secondSpinner.getEditor().setText("0");
            }
        }));
        secondSpinner.setEditable(true);

        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            LocalDateTime localDateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourValueFactory.getValue(), minuteValueFactory.getValue(), secondValueFactory.getValue()));
            expression.set(new SimpleRTCExpression(new RealTimeClock(RealTimeClock.Mode.SPECIFIC, localDateTime)));
        };
        datePicker.valueProperty().addListener(changeListener);
        hourValueFactory.valueProperty().addListener(changeListener);
        minuteValueFactory.valueProperty().addListener(changeListener);
        secondValueFactory.valueProperty().addListener(changeListener);

        setSpacing(5.0);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(datePicker, hourSpinner, new Label(":"), minuteSpinner, new Label(":"), secondSpinner);
        getStylesheets().add(this.getClass().getResource("/css/canvas/node/expressioncontrol/RTCExpressionControl.css").toExternalForm());
    }

    public ReadOnlyObjectProperty<SimpleRTCExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
