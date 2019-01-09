package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ConditionalExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.Operator;
import io.makerplayground.ui.canvas.node.expression.custom.ChipField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CustomConditionalExpressionControl extends VBox {

    private final ConditionalExpression expression;
    private final ObservableList<ProjectValue> projectValues;
    private final List<EntryView> entryViewList;

    public CustomConditionalExpressionControl(ConditionalExpression expression, ObservableList<ProjectValue> projectValues) {
        this.expression = expression;
        this.projectValues = projectValues;
        this.entryViewList = new ArrayList<>();

        Button addButton = new Button("+");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            ConditionalExpression.Entry entry = new ConditionalExpression.Entry(Operator.GREATER_THAN, new CustomNumberExpression());
            expression.getEntries().add(entry);
            createExpressionRowControl(entry);
        });
        getChildren().add(addButton);

        for (ConditionalExpression.Entry entry : expression.getEntries()) {
            createExpressionRowControl(entry);
        }

        setSpacing(5);
    }

    private void createExpressionRowControl(ConditionalExpression.Entry entry) {
        EntryView entryView = new EntryView(entry, projectValues);
        entryView.setOnRemoveButtonPressed(event -> {
            expression.getEntries().remove(entry);
            getChildren().remove(entryView);
            entryViewList.remove(entryView);
            invalidateView();
        });
        getChildren().add(getChildren().size() - 1, entryView);
        entryViewList.add(entryView);
        invalidateView();
    }

    private void invalidateView() {
        for (EntryView view : entryViewList) {
            view.setOperatorVisible(true);
            view.setRemoveButtonVisible(true);
        }
        // hide 'and' label of the last expression entry
        entryViewList.get(entryViewList.size() - 1).setOperatorVisible(false);
        // hide remove button when there is only 1 entry in the expression
        if (entryViewList.size() == 1) {
            entryViewList.get(0).setRemoveButtonVisible(false);
        }
    }

    private static class EntryView extends HBox {

        private Button removeButton;
        private Label operatorLabel;

        public EntryView(ConditionalExpression.Entry entry, ObservableList<ProjectValue> projectValues) {
            ComboBox<Operator> operatorComboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.getComparisonOperator()));
            if (entry.getOperator() != null) {
                operatorComboBox.setValue(entry.getOperator());
            }
            operatorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                entry.setOperator(newValue);
            });

            ChipField chipField = new ChipField(entry.getExpression(), projectValues);
            chipField.expressionProperty().addListener((observable, oldValue, newValue) -> entry.setExpression(newValue));

            operatorLabel = new Label("and");
            operatorLabel.setMinHeight(25); // a hack to center the label to the height of 1 row control when the control spans to multiple rows
            operatorLabel.managedProperty().bind(operatorLabel.visibleProperty());

            removeButton = new Button("-");
            removeButton.managedProperty().bind(removeButton.visibleProperty());

            setAlignment(Pos.TOP_LEFT);
            setSpacing(5);
            getChildren().addAll(operatorComboBox, chipField, removeButton, operatorLabel);
        }

        void setOnRemoveButtonPressed(EventHandler<ActionEvent> e) {
            removeButton.setOnAction(e);
        }

        void setRemoveButtonVisible(boolean b) {
            removeButton.setVisible(b);
        }

        void setOperatorVisible(boolean b) {
            operatorLabel.setVisible(b);
        }
    }

    public ConditionalExpression getExpression() {
        return expression;
    }
}
