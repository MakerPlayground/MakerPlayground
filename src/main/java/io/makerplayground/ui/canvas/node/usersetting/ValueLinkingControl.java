package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ValueLinkingExpression;
import io.makerplayground.ui.canvas.node.usersetting.expression.RangeSliderWithUnit;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import java.util.List;

public class ValueLinkingControl extends GridPane {

    private final ReadOnlyObjectWrapper<ValueLinkingExpression> expression;
    private final List<ProjectValue> projectValues;

    private ComboBox<ProjectValue> valueCombobox;
    private RangeSliderWithUnit sourceRange;
    private RangeSliderWithUnit destRange;

    public ValueLinkingControl(ValueLinkingExpression expression, List<ProjectValue> projectValues) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.projectValues = projectValues;
        initView();
        initEvent();
    }

    private void initView() {
        Label fromLabel = new Label("From");
        GridPane.setRowIndex(fromLabel, 0);
        GridPane.setColumnIndex(fromLabel, 0);

        valueCombobox = new ComboBox<>(FXCollections.observableList(projectValues));
        if (getExpression().getSourceValue() != null) {
            valueCombobox.getSelectionModel().select(getExpression().getSourceValue());
        }
        valueCombobox.setCellFactory(param -> new ListCell<>(){
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                }
            }
        });
        valueCombobox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                }
            }
        });
        GridPane.setRowIndex(valueCombobox, 0);
        GridPane.setColumnIndex(valueCombobox, 1);

        sourceRange = new RangeSliderWithUnit();
        sourceRange.disableProperty().bind(valueCombobox.getSelectionModel().selectedItemProperty().isNull());
        if (getExpression().getSourceValue() != null) {
            NumericConstraint constraint = (NumericConstraint) getExpression().getSourceValue().getValue().getConstraint();
            sourceRange.setMaxValue(new NumberWithUnit(constraint.getMax(), constraint.getUnit()));
            sourceRange.setMinValue(new NumberWithUnit(constraint.getMin(), constraint.getUnit()));
            sourceRange.setHighValue(getExpression().getSourceHighValue());
            sourceRange.setLowValue(getExpression().getSourceLowValue());
        }
        GridPane.setRowIndex(sourceRange, 1);
        GridPane.setColumnIndex(sourceRange, 1);

        Label toLabel = new Label("To " + getExpression().getDestinationParameter().getName());
        GridPane.setRowIndex(toLabel, 2);
        GridPane.setColumnIndex(toLabel, 0);

        destRange = new RangeSliderWithUnit();
        Parameter p = getExpression().getDestinationParameter();
        destRange.setMaxValue(new NumberWithUnit(p.getMaximumValue(), p.getUnit().get(0)));
        destRange.setMinValue(new NumberWithUnit(p.getMinimumValue(), p.getUnit().get(0)));
        destRange.setHighValue(getExpression().getDestinationHighValue());
        destRange.setLowValue(getExpression().getDestinationLowValue());
        GridPane.setRowIndex(destRange, 2);
        GridPane.setColumnIndex(destRange, 1);

        setHgap(5);
        setVgap(5);
        getChildren().addAll(fromLabel, valueCombobox, sourceRange, toLabel, destRange);
    }

    private void initEvent() {
        valueCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // it is necessary to store the new expression in local variable instead of calling getExpression() in this
            // listener as calling sourceRange.setHighValue() will trigger change listener of sourceRange.highValueProperty()
            // causing new expression to be created (by deep copying the current expression and only change it's high value)
            // thus we will lost the new low value
            ValueLinkingExpression newExpression = getExpression().setSourceValue(newValue);
            expression.set(newExpression);

            NumericConstraint constraint = (NumericConstraint) newExpression.getSourceValue().getValue().getConstraint();
            sourceRange.setMaxValue(new NumberWithUnit(constraint.getMax(), constraint.getUnit()));
            sourceRange.setMinValue(new NumberWithUnit(constraint.getMin(), constraint.getUnit()));
            sourceRange.setHighValue(newExpression.getSourceHighValue());
            sourceRange.setLowValue(newExpression.getSourceLowValue());
        });
        sourceRange.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(getExpression().setSourceLowValue(newValue));
        });
        sourceRange.highValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(getExpression().setSourceHighValue(newValue));
        });
        destRange.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(getExpression().setDestinationLowValue(newValue));
        });
        destRange.highValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(getExpression().setDestinationHighValue(newValue));
        });
    }

    public ValueLinkingExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<ValueLinkingExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
