package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ConditionalExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.Operator;
import io.makerplayground.ui.canvas.node.expression.custom.ChipField;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.List;

public class CustomConditionalExpressionControl extends GridPane {

    private final ConditionalExpression expression;
    private final List<ProjectValue> projectValues;

    private int currentRow = 0;

    public CustomConditionalExpressionControl(ConditionalExpression expression, List<ProjectValue> projectValues) {
        this.expression = expression;
        this.projectValues = projectValues;

        for (ConditionalExpression.Entry entry : expression.getEntries()) {
            createExpressionRowControl(entry);
        }

        Button addButton = new Button("+");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            ConditionalExpression.Entry entry = new ConditionalExpression.Entry(Operator.GREATER_THAN, new CustomNumberExpression());
            expression.getEntries().add(entry);
            createExpressionRowControl(entry);
            GridPane.setRowIndex(addButton, currentRow);
            currentRow++;
        });
        GridPane.setConstraints(addButton, 0, currentRow, 2, 1);
        getChildren().add(addButton);
        currentRow++;

        setHgap(5);
    }


    private void createExpressionRowControl(ConditionalExpression.Entry entry) {
        ComboBox<Operator> operatorComboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.getComparisonOperator()));
        if (entry.getOperator() != null) {
            operatorComboBox.setValue(entry.getOperator());
        }
        operatorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            entry.setOperator(newValue);
        });
        GridPane.setConstraints(operatorComboBox, 0, currentRow, 1, 1, HPos.CENTER, VPos.TOP);

        ChipField chipField = new ChipField(entry.getExpression(), projectValues);
        chipField.setRefreshIntervalAdjustable(false);
        chipField.expressionProperty().addListener((observable, oldValue, newValue) -> entry.setExpression(newValue));
        GridPane.setConstraints(chipField, 1, currentRow);

        // we add this node to reserve some spaces in the next row and remove it when this row is removed because
        // the vgap is also applied to the empty row emerged after we remove some rows in the middle (updating the
        // constraint of every row behind is hard so we do it this way)
        Pane spring = new Pane();
        spring.setMinHeight(5);
        GridPane.setConstraints(spring, 0, currentRow+1);

        Button removeButton = new Button("-");
        removeButton.setOnAction(event -> {
            getChildren().removeAll(operatorComboBox, chipField, removeButton, spring);
            getExpression().getEntries().remove(entry);
        });
        GridPane.setConstraints(removeButton, 2, currentRow, 1, 1, HPos.CENTER, VPos.TOP);

        getChildren().addAll(operatorComboBox, chipField, removeButton, spring);
        currentRow += 2;
    }

    public ConditionalExpression getExpression() {
        return expression;
    }
}
