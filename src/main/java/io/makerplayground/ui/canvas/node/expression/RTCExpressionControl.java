package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.project.expression.SimpleRTCExpression;
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
            }
            else {
                // TODO: Support mode NOW
                System.err.println("No implementation for NOW mode right now.");
            }
        }
        Label dateLabel = new Label("Date");
        DatePicker datePicker = new DatePicker(datetime.toLocalDate());
        Label timeLabel = new Label("Time");
        // Note that we need to set the converter before value.
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
        getChildren().addAll(dateLabel, datePicker, timeLabel, spinnerHH, spinnerMM, spinnerSS);
    }

    public ReadOnlyObjectWrapper<SimpleRTCExpression> expressionProperty() {
        return expression;
    }
}
