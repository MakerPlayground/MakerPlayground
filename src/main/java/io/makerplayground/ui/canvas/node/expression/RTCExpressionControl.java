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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RTCExpressionControl extends VBox {
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
                e.printStackTrace();
                return 0;
            }
        }
    };

    private ReadOnlyObjectWrapper<SimpleRTCExpression> expression;

    public RTCExpressionControl(SimpleRTCExpression expression) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        initControl();
    }

    private void initControl() {
        setSpacing(5.0);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().clear();
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

        // Note that we need to set the converter before value.
        HBox secondRow = new HBox(5.0);
        Spinner<Integer> spinnerHH = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23));
        spinnerHH.getValueFactory().setConverter(twoDigitStringConverter);
        spinnerHH.getValueFactory().setValue(datetime.getHour());
        Spinner<Integer> spinnerMM = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        spinnerMM.getValueFactory().setConverter(twoDigitStringConverter);
        spinnerMM.getValueFactory().setValue(datetime.getMinute());
        Spinner<Integer> spinnerSS = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        spinnerSS.getValueFactory().setConverter(twoDigitStringConverter);
        spinnerSS.getValueFactory().setValue(datetime.getSecond());
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            LocalDateTime localDateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.of(spinnerHH.getValue(), spinnerMM.getValue(), spinnerSS.getValue()));
            expression.set(new SimpleRTCExpression(new RealTimeClock(RealTimeClock.Mode.SPECIFIC, localDateTime)));
        };
        datePicker.valueProperty().addListener(changeListener);
        spinnerHH.valueProperty().addListener(changeListener);
        spinnerMM.valueProperty().addListener(changeListener);
        spinnerSS.valueProperty().addListener(changeListener);

        secondRow.getChildren().addAll(spinnerHH, spinnerMM, spinnerSS);

        getChildren().addAll(datePicker, secondRow);
    }

    public ReadOnlyObjectWrapper<SimpleRTCExpression> expressionProperty() {
        return expression;
    }
}
