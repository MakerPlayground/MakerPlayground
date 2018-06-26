package io.makerplayground.ui.canvas;

import io.makerplayground.project.term.Term;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StringChip extends Chip<String> {
    private static final Color BACKGROUND_COLOR = Color.DARKORANGE;
    private static final Color BACKGROUND_COLOR_SELECTED = Color.ORANGERED;

    public StringChip() {
        super("", Term.Type.STRING);
    }

    public StringChip(String initialValue) {
        super(initialValue, Term.Type.STRING);
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
        input.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        input.setAlignment(Pos.BASELINE_CENTER);
        input.setPrefSize(40, 20);
        input.setMaxSize(40, 20);
        input.textProperty().bindBidirectional(valueProperty());

        getChildren().addAll(background, input);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public ObjectProperty<String> valueProperty() {
        return super.valueProperty();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }
}
