package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.io.IOException;

public class OperatorChip extends Chip<OperatorTerm.Operator> {

    @FXML private Path background;
    @FXML private Text text;

    public OperatorChip(OperatorTerm.Operator initialValue) {
        super(initialValue, Term.Type.OPERATOR);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/Operator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        text.setText(getValue().toString());

        if (getValue().getType() == OperatorTerm.OperatorType.LEFT_UNARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new ArcTo(10, 10, 90, 0, 10, false, false)
                    , new LineTo(0, 15)
                    , new ArcTo(10, 10, 0, 10, 25, false, false)
                    , new LineTo(25, 25)
                    , new ArcTo(10, 10, 0, 15, 15, false, true)
                    , new LineTo(15, 10)
                    , new ArcTo(10, 10, 0, 25, 0, false, true)
                    , new ClosePath());
        } else if (getValue().getType() == OperatorTerm.OperatorType.BINARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(10, 10, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(10, 10, 0, 0, 25, false, true)
                    , new LineTo(35, 25)
                    , new ArcTo(10, 10, 0, 25, 15, false, true)
                    , new LineTo(25, 10)
                    , new ArcTo(10, 10, 0, 35, 0, false, true)
                    , new ClosePath());
        } else if (getValue().getType() == OperatorTerm.OperatorType.RIGHT_UNARY) {
            background.getElements().addAll(new MoveTo(15, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(10, 10, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(10, 10, 0, 0, 25, false, true)
                    , new LineTo(15, 25)
                    , new ArcTo(10, 10, 0, 25, 15, false, false)
                    , new LineTo(25, 10)
                    , new ArcTo(10, 10, 0, 15, 0, false, false)
                    , new ClosePath());
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public OperatorTerm getTerm() {
        return new OperatorTerm(getValue());
    }

    @Override
    public OperatorTerm.Operator getValue() {
        return super.getValue();
    }

    @Override
    public ObjectProperty<OperatorTerm.Operator> valueProperty() {
        throw new UnsupportedOperationException("OperatorChip should not be edited");
    }

    @Override
    public void setValue(OperatorTerm.Operator value) {
        throw new UnsupportedOperationException("OperatorChip should not be edited");
    }
}
