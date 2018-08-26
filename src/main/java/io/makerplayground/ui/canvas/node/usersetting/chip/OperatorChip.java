package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Operator;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.OperatorType;
import io.makerplayground.project.term.Term;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.io.IOException;

public class OperatorChip extends Chip<Operator> {

    @FXML private Path background;
    @FXML private Text text;

    public OperatorChip(Operator initialValue) {
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

        if (getValue().getType() == OperatorType.LEFT_UNARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new ArcTo(12.5, 12.5, 90, 0, 10, false, false)
                    , new LineTo(0, 15)
                    , new ArcTo(12.5, 12.5, 0, 10, 25, false, false)
                    , new LineTo(25, 25)
                    , new ArcTo(12.5, 12.5, 0, 15, 15, false, true)
                    , new LineTo(15, 10)
                    , new ArcTo(12.5, 12.5, 0, 25, 0, false, true)
                    , new ClosePath());
        } else if (getValue().getType() == OperatorType.BINARY) {
            background.getElements().addAll(new MoveTo(10, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(12.5, 12.5, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(12.5, 12.5, 0, 0, 25, false, true)
                    , new LineTo(35, 25)
                    , new ArcTo(12.5, 12.5, 0, 25, 15, false, true)
                    , new LineTo(25, 10)
                    , new ArcTo(12.5, 12.5, 0, 35, 0, false, true)
                    , new ClosePath());
        } else if (getValue().getType() == OperatorType.RIGHT_UNARY) {
            background.getElements().addAll(new MoveTo(15, 0)
                    , new LineTo(0, 0)
                    , new ArcTo(12.5, 12.5, 90, 10, 10, false, true)
                    , new LineTo(10, 15)
                    , new ArcTo(12.5, 12.5, 0, 0, 25, false, true)
                    , new LineTo(15, 25)
                    , new ArcTo(12.5, 12.5, 0, 25, 15, false, false)
                    , new LineTo(25, 10)
                    , new ArcTo(12.5, 12.5, 0, 15, 0, false, false)
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
    protected void updateChipStyle(boolean selected) {
        if (!selected) {
            background.setFill(Color.web("#ff8b01"));
        } else {
            background.setFill(Color.web("#ff8b01").darker());
        }
    }
}
