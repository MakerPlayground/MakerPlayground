package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.PopOver;

import java.io.IOException;

public class ChipSelectorPopover extends PopOver {

    @FXML private GridPane gridPane = new GridPane();
    @FXML private StackPane numberChip;
    @FXML private StackPane valueChip;
    @FXML private StackPane plusChip;
    @FXML private StackPane multiplyChip;
    @FXML private StackPane minusChip;
    @FXML private StackPane divideChip;
    @FXML private StackPane openParenthesisChip;
    @FXML private StackPane closeParenthesisChip;

    private ChipSelectorListener listener;

    public ChipSelectorPopover() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ChipSelectorPopover.fxml"));
        fxmlLoader.setRoot(gridPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        numberChip.setOnMousePressed(event -> fireEvent(new NumberWithUnitTerm(NumberWithUnit.ZERO)));
        valueChip.setOnMousePressed(event -> fireEvent(new ValueTerm(null)));
        plusChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.PLUS)));
        multiplyChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.MULTIPLY)));
        minusChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.MINUS)));
        divideChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.DIVIDE)));
        openParenthesisChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.OPEN_PARENTHESIS)));
        closeParenthesisChip.setOnMousePressed(event -> fireEvent(new OperatorTerm(Operator.CLOSE_PARENTHESIS)));

        setContentNode(gridPane);
    }

    private void fireEvent(Term t) {
        if (listener != null) {
            listener.chipSelected(t);
        }
    }

    public void setOnChipSelected(ChipSelectorListener listener) {
        this.listener = listener;
    }
}
