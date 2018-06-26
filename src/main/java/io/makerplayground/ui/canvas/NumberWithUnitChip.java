package io.makerplayground.ui.canvas;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.chip.Term;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class NumberWithUnitChip extends Chip<NumberWithUnit> {
    private static final Color BACKGROUND_COLOR = Color.DARKRED;
    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public NumberWithUnitChip() {
        super(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED), Term.Type.NUMBER);
    }

    public NumberWithUnitChip(NumberWithUnit initialValue) {
        super(initialValue, Term.Type.NUMBER);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setWidth(40);
        background.setHeight(20);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().bind(Bindings.when(selectedProperty())
                .then(BACKGROUND_COLOR_SELECTED).otherwise(BACKGROUND_COLOR));

        TextField input = new TextField();
        input.setText(String.valueOf(getValue()));
        input.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        input.setAlignment(Pos.BASELINE_CENTER);
        input.setPrefSize(40, 20);
        input.setMaxSize(40, 20);
        input.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    double value = Double.parseDouble(input.getText());
                    setValue(new NumberWithUnit(value, getValue().getUnit()));  // TODO: shouldn't hardcoded unit
                } catch (NumberFormatException e) {
                    input.setText(String.valueOf(getValue()));
                }
            }
        });

        getChildren().addAll(background, input);
    }

    @Override
    public NumberWithUnit getValue() {
        return super.getValue();
    }

    @Override
    public ObjectProperty<NumberWithUnit> valueProperty() {
        return super.valueProperty();
    }

    @Override
    public void setValue(NumberWithUnit value) {
        super.setValue(value);
    }
}
