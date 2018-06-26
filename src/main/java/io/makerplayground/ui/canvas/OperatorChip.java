package io.makerplayground.ui.canvas;

import io.makerplayground.project.chip.OperatorTerm;
import io.makerplayground.project.chip.Term;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class OperatorChip extends Chip<OperatorTerm.OP> {
    private static final Color BACKGROUND_COLOR = Color.DARKGREEN;
    private static final Color BACKGROUND_COLOR_SELECTED = Color.GREEN;

    public static OperatorChip PLUS = new OperatorChip(OperatorTerm.OP.PLUS);
    public static OperatorChip MINUS = new OperatorChip(OperatorTerm.OP.MINUS);
    public static OperatorChip MULTIPLY = new OperatorChip(OperatorTerm.OP.MULTIPLY);
    public static OperatorChip DIVIDE = new OperatorChip(OperatorTerm.OP.DIVIDE);

    public static OperatorChip GREATER_THAN = new OperatorChip(OperatorTerm.OP.GREATER_THAN);
    public static OperatorChip LESS_THAN = new OperatorChip(OperatorTerm.OP.LESS_THAN);
    public static OperatorChip GREATER_THAN_OR_EQUAL = new OperatorChip(OperatorTerm.OP.GREATER_THAN_OR_EQUAL);
    public static OperatorChip LESS_THAN_OR_EQUAL = new OperatorChip(OperatorTerm.OP.LESS_THAN_OR_EQUAL);

    public static OperatorChip AND = new OperatorChip(OperatorTerm.OP.AND);
    public static OperatorChip OR = new OperatorChip(OperatorTerm.OP.OR);
    public static OperatorChip NOT = new OperatorChip(OperatorTerm.OP.NOT);
    public static OperatorChip OPEN_PARENTHESIS = new OperatorChip(OperatorTerm.OP.OPEN_PARENTHESIS);
    public static OperatorChip CLOSE_PARENTHESIS = new OperatorChip(OperatorTerm.OP.CLOSE_PARENTHESIS);

    private OperatorChip(OperatorTerm.OP initialValue) {
        super(initialValue, Term.Type.OPERATOR);
    }

    public static OperatorChip getInstance(OperatorTerm.OP operator) {
        switch (operator) {
            case PLUS:
                return PLUS;
            case MINUS:
                return MINUS;
            case MULTIPLY:
                return MULTIPLY;
            case DIVIDE:
                return DIVIDE;
            case GREATER_THAN:
                return GREATER_THAN;
            case LESS_THAN:
                return LESS_THAN;
            case GREATER_THAN_OR_EQUAL:
                return GREATER_THAN_OR_EQUAL;
            case LESS_THAN_OR_EQUAL:
                return LESS_THAN_OR_EQUAL;
            case AND:
                return AND;
            case OR:
                return OR;
            case NOT:
                return NOT;
            case OPEN_PARENTHESIS:
                return OPEN_PARENTHESIS;
            case CLOSE_PARENTHESIS:
                return CLOSE_PARENTHESIS;
            default:
                throw new IllegalStateException("Unknown enum constant");
        }
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setWidth(25);
        background.setHeight(20);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().bind(Bindings.when(selectedProperty())
                .then(BACKGROUND_COLOR_SELECTED).otherwise(BACKGROUND_COLOR));

        Label label = new Label();
        label.setText(getValue().toString());
        label.setStyle("-fx-text-fill: white;");
        label.setAlignment(Pos.BASELINE_CENTER);

        getChildren().addAll(background, label);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        // consume to prevent ChipField from deselect this chip immediately
        addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);
    }

    @Override
    public OperatorTerm.OP getValue() {
        return super.getValue();
    }

    @Override
    public ObjectProperty<OperatorTerm.OP> valueProperty() {
        throw new UnsupportedOperationException("OperatorChip should'h not be edited");
    }

    @Override
    public void setValue(OperatorTerm.OP value) {
        throw new UnsupportedOperationException("OperatorChip should'h not be edited");
    }
}
