package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.Term;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class NumberWithUnitChip extends Chip<NumberWithUnit> {
//    private static final Color BACKGROUND_COLOR = Color.DARKRED;
    private static final Color BACKGROUND_COLOR = Color.valueOf("081e42");
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public NumberWithUnitChip(NumberWithUnit initialValue) {
        super(initialValue, Term.Type.NUMBER);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().setValue(BACKGROUND_COLOR);

        TextField input = new TextField();
        input.setText(String.valueOf(getValue()));
        input.setAlignment(Pos.BASELINE_CENTER);
//        input.setPrefSize(30, 20);
//        input.setMaxSize(30, 20);
        input.setStyle("-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-text-fill: white;");
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
        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        }));
        setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
    }

    @Override
    public Term getTerm() {
        return new NumberWithUnitTerm(getValue());
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
