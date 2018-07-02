package io.makerplayground.ui.canvas.chip;

import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class OperatorChip extends Chip<OperatorTerm.OP> {
    private static final Color BACKGROUND_COLOR = Color.DARKGREEN;
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.GREEN;

    public OperatorChip(OperatorTerm.OP initialValue) {
        super(initialValue, Term.Type.OPERATOR);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setWidth(25);
        background.setHeight(20);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().setValue(BACKGROUND_COLOR);

        Text text = new Text(getValue().toString());
        text.setFill(Color.WHITE);
        getChildren().addAll(background, text);
        setMaxSize(25,20);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
//        // consume to prevent ChipField from deselect this chip immediately
//        addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);
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
