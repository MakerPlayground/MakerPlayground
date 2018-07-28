package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.ValueTerm;
import io.makerplayground.ui.canvas.node.usersetting.chip.ChipField;
import io.makerplayground.ui.canvas.node.usersetting.chip.LabelChip;
import io.makerplayground.ui.canvas.node.usersetting.chip.OperatorChip;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.controlsfx.control.PopOver;

public class NumberWithUnitPopOver extends PopOver {

    public NumberWithUnitPopOver(ChipField chipField) {
        FlowPane chipPane = new FlowPane();
        chipPane.setHgap(3);
        chipPane.setVgap(5);
        chipPane.setPrefWrapLength(180);
        chipPane.setPadding(new Insets(15));
//        chipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        LabelChip labelChipNumber = new LabelChip("number");
        LabelChip labelChipValue = new LabelChip("value");

        OperatorChip plusChip = new OperatorChip(OperatorTerm.OP.PLUS);
        OperatorChip minusChip = new OperatorChip(OperatorTerm.OP.MINUS);
        OperatorChip multiplyChip = new OperatorChip(OperatorTerm.OP.MULTIPLY);
        OperatorChip divideChip = new OperatorChip(OperatorTerm.OP.DIVIDE);
        OperatorChip openParenChip = new OperatorChip(OperatorTerm.OP.OPEN_PARENTHESIS);
        OperatorChip closeParenChip = new OperatorChip(OperatorTerm.OP.CLOSE_PARENTHESIS);

        labelChipNumber.setOnMousePressed(event -> chipField.addChip(new NumberWithUnitTerm(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED))));
        labelChipValue.setOnMousePressed((event -> chipField.addChip(new ValueTerm(null))));
        plusChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.PLUS)));
        minusChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.MINUS)));
        multiplyChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.MULTIPLY)));
        divideChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.DIVIDE)));
        openParenChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.OPEN_PARENTHESIS)));
        closeParenChip.setOnMousePressed(event -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.CLOSE_PARENTHESIS)));

        chipPane.getChildren().add(labelChipNumber);
        chipPane.getChildren().add(labelChipValue);
        chipPane.getChildren().add(plusChip);
        chipPane.getChildren().add(minusChip);
        chipPane.getChildren().add(multiplyChip);
        chipPane.getChildren().add(divideChip);
        chipPane.getChildren().add(openParenChip);
        chipPane.getChildren().add(closeParenChip);

//        SplitPane splitPane = new SplitPane(chipPane, chipPane);
//        splitPane.setOrientation(Orientation.HORIZONTAL);
//        splitPane.setDividerPositions(0.5);

        setContentNode(chipPane);

//        FlowPane operandChipPane = new FlowPane();
//        operandChipPane.setHgap(3);
//        operandChipPane.setVgap(5);
//        operandChipPane.setPrefSize(200, 100);
//        operandChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//
//        LabelChip labelChipNumber = new LabelChip("number");
//        operandChipPane.getChildren().add(labelChipNumber);
//        labelChipNumber.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new NumberWithUnitTerm(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED)))));
//
//        LabelChip labelChipValue = new LabelChip("value");
//        operandChipPane.getChildren().add(labelChipValue);
//        labelChipValue.setOnMousePressed((event -> Platform.runLater(() -> chipField.addChip(new ValueTerm(null)))));
//
//        FlowPane operatorChipPane = new FlowPane();
//        operatorChipPane.setHgap(3);
//        operatorChipPane.setVgap(5);
//        operatorChipPane.setPrefSize(200, 100);
//        operatorChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//        OperatorChip plusChip = new OperatorChip(OperatorTerm.OP.PLUS);
//        OperatorChip minusChip = new OperatorChip(OperatorTerm.OP.MINUS);
//        OperatorChip multiplyChip = new OperatorChip(OperatorTerm.OP.MULTIPLY);
//        OperatorChip divideChip = new OperatorChip(OperatorTerm.OP.DIVIDE);
//        OperatorChip openParenChip = new OperatorChip(OperatorTerm.OP.OPEN_PARENTHESIS);
//        OperatorChip closeParenChip = new OperatorChip(OperatorTerm.OP.CLOSE_PARENTHESIS);
//
//        plusChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.PLUS))));
//        minusChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.MINUS))));
//        multiplyChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.MULTIPLY))));
//        divideChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.DIVIDE))));
//        openParenChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.OPEN_PARENTHESIS))));
//        closeParenChip.setOnMousePressed(event -> Platform.runLater(() -> chipField.addChip(new OperatorTerm(OperatorTerm.OP.CLOSE_PARENTHESIS))));
//
//        operatorChipPane.getChildren().add(plusChip);
//        operatorChipPane.getChildren().add(minusChip);
//        operatorChipPane.getChildren().add(multiplyChip);
//        operatorChipPane.getChildren().add(divideChip);
//        operatorChipPane.getChildren().add(openParenChip);
//        operatorChipPane.getChildren().add(closeParenChip);
//
//        SplitPane splitPane = new SplitPane(operandChipPane, operatorChipPane);
//        splitPane.setOrientation(Orientation.HORIZONTAL);
//        splitPane.setDividerPositions(0.5);
//
//        setContentNode(splitPane);
    }
}
