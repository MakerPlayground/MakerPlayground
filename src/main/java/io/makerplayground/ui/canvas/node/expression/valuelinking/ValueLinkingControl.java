/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas.node.expression.valuelinking;

import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.ProjectValueExpression;
import io.makerplayground.project.expression.ValueLinkingExpression;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.RangeSliderWithUnit;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SpinnerWithUnit;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class ValueLinkingControl extends GridPane {

    private final ReadOnlyObjectWrapper<Expression> expression;
    private final List<ProjectValue> projectValues;
    private final Parameter parameter;

    private ComboBox<ProjectValue> valueCombobox;
    private RangeSliderWithUnit sourceRange;
    private RangeSliderWithUnit destRange;
    private ChangeListener<Boolean> mappingEnabledChangeListener;

    public ValueLinkingControl(Expression expression, List<ProjectValue> projectValues, Parameter parameter) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.projectValues = projectValues;
        this.parameter = parameter;
        this.mappingEnabledChangeListener = (observable, oldValue, newValue) -> {
            if (newValue) {
                ValueLinkingExpression valueLinkingExpression = new ValueLinkingExpression(parameter)
                        .setSourceValue(((ProjectValueExpression) getExpression()).getProjectValue());
                valueLinkingExpression.setRefreshInterval(getExpression().getRefreshInterval());
                valueLinkingExpression.setUserDefinedInterval(getExpression().getUserDefinedInterval());
                this.expression.set(valueLinkingExpression);
                initValueLinkingControl();
            } else {
                ProjectValueExpression projectValueExpression = new ProjectValueExpression()
                        .setProjectValue(((ValueLinkingExpression) getExpression()).getSourceValue());
                projectValueExpression.setRefreshInterval(getExpression().getRefreshInterval());
                projectValueExpression.setUserDefinedInterval(getExpression().getUserDefinedInterval());
                this.expression.set(projectValueExpression);
                initValueControl();
            }
        };
        if (expression instanceof ProjectValueExpression) {
            initValueControl();
        } else if (expression instanceof ValueLinkingExpression) {
            initValueLinkingControl();
        }
    }

    private void initValueControl() {
        getChildren().clear();

        ProjectValueExpression projectValueExpression = (ProjectValueExpression) getExpression();

        Label updateLabel = new Label("update");
        GridPane.setConstraints(updateLabel, 0, 1);

        ComboBox<Expression.RefreshInterval> refreshIntervalComboBox = new ComboBox<>(FXCollections.observableArrayList(Expression.RefreshInterval.values()));
        refreshIntervalComboBox.getSelectionModel().select(getExpression().getRefreshInterval());
        refreshIntervalComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> getExpression().setRefreshInterval(newValue));

        SpinnerWithUnit customIntervalSpinner = new SpinnerWithUnit(0, Double.MAX_VALUE, List.of(Unit.SECOND, Unit.MILLISECOND), getExpression().getUserDefinedInterval());
        customIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) ->
                getExpression().setUserDefinedInterval(newValue));
        customIntervalSpinner.visibleProperty().bind(refreshIntervalComboBox.getSelectionModel().selectedItemProperty().isEqualTo(Expression.RefreshInterval.USER_DEFINED));
        customIntervalSpinner.managedProperty().bind(customIntervalSpinner.visibleProperty());

        HBox refreshIntervalHBox = new HBox(5);
        refreshIntervalHBox.getChildren().addAll(refreshIntervalComboBox, customIntervalSpinner);
        GridPane.setConstraints(refreshIntervalHBox, 1, 1, 2, 1);

        Label fromLabel = new Label("set to");
        GridPane.setConstraints(fromLabel, 0, 0);

        valueCombobox = new ComboBox<>(FXCollections.observableList(projectValues));
        if (projectValueExpression.getProjectValue() != null) {
            valueCombobox.getSelectionModel().select(projectValueExpression.getProjectValue());
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
        valueCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                expression.set(((ProjectValueExpression) expression.get()).setProjectValue(newValue)));
        GridPane.setConstraints(valueCombobox, 1, 0);

        CheckBox mappingEnableCheckbox = new CheckBox("map range");
        mappingEnableCheckbox.selectedProperty().addListener(mappingEnabledChangeListener);
        GridPane.setConstraints(mappingEnableCheckbox, 2, 0);

        setHgap(10);
        setVgap(5);
        getChildren().addAll(fromLabel, valueCombobox, mappingEnableCheckbox, updateLabel, refreshIntervalHBox);
    }

    private void initValueLinkingControl() {
        getChildren().clear();

        ValueLinkingExpression valueLinkingExpression = (ValueLinkingExpression) getExpression();

        Label updateLabel = new Label("update");
        GridPane.setConstraints(updateLabel, 0, 3);

        ComboBox<Expression.RefreshInterval> refreshIntervalComboBox = new ComboBox<>(FXCollections.observableArrayList(Expression.RefreshInterval.values()));
        refreshIntervalComboBox.getSelectionModel().select(getExpression().getRefreshInterval());
        refreshIntervalComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                getExpression().setRefreshInterval(newValue));

        SpinnerWithUnit customIntervalSpinner = new SpinnerWithUnit(0, Double.MAX_VALUE, List.of(Unit.SECOND, Unit.MILLISECOND), getExpression().getUserDefinedInterval());
        customIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) ->
                getExpression().setUserDefinedInterval(newValue));
        customIntervalSpinner.visibleProperty().bind(refreshIntervalComboBox.getSelectionModel().selectedItemProperty().isEqualTo(Expression.RefreshInterval.USER_DEFINED));
        customIntervalSpinner.managedProperty().bind(customIntervalSpinner.visibleProperty());

        HBox refreshIntervalHBox = new HBox(5);
        refreshIntervalHBox.getChildren().addAll(refreshIntervalComboBox, customIntervalSpinner);
        GridPane.setConstraints(refreshIntervalHBox, 1, 3, 2, 1);

        Label fromLabel = new Label("set to");
        GridPane.setConstraints(fromLabel, 0, 0);

        HBox valueSelectionHBox = new HBox(10);

        valueCombobox = new ComboBox<>(FXCollections.observableList(projectValues));
        if (valueLinkingExpression.getSourceValue() != null) {
            valueCombobox.getSelectionModel().select(valueLinkingExpression.getSourceValue());
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
        valueCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // it is necessary to store the new expression in local variable instead of calling getExpression() in this
            // listener as calling sourceRange.setHighValue() will trigger change listener of sourceRange.highValueProperty()
            // causing new expression to be created (by deep copying the current expression and only change it's high value)
            // thus we will lost the new low value
            ValueLinkingExpression newExpression = ((ValueLinkingExpression) getExpression()).setSourceValue(newValue);
            expression.set(newExpression);

            NumericConstraint constraint = (NumericConstraint) newExpression.getSourceValue().getValue().getConstraint();
            sourceRange.setMaxValue(new NumberWithUnit(constraint.getMax(), constraint.getUnit()));
            sourceRange.setMinValue(new NumberWithUnit(constraint.getMin(), constraint.getUnit()));
            sourceRange.setHighValue(newExpression.getSourceHighValue());
            sourceRange.setLowValue(newExpression.getSourceLowValue());
        });

        CheckBox mappingEnableCheckbox = new CheckBox("map range");
        mappingEnableCheckbox.setMinHeight(25.0);
        mappingEnableCheckbox.setSelected(true);
        mappingEnableCheckbox.selectedProperty().addListener(mappingEnabledChangeListener);

        CheckBox inverseEnableCheckbox = new CheckBox("inverse");
        inverseEnableCheckbox.setMinHeight(25.0);
        inverseEnableCheckbox.setSelected(valueLinkingExpression.isInverse());
        inverseEnableCheckbox.disableProperty().bind(mappingEnableCheckbox.selectedProperty().not());
        inverseEnableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            sourceRange.setInverse(inverseEnableCheckbox.isSelected());
            expression.set(((ValueLinkingExpression) getExpression()).setInverse(inverseEnableCheckbox.isSelected()));
        });
        valueSelectionHBox.getChildren().addAll(valueCombobox, mappingEnableCheckbox, inverseEnableCheckbox);
        GridPane.setConstraints(valueSelectionHBox, 1, 0, 2, 1);

        Label fromRangeLabel = new Label("from range");
        GridPane.setConstraints(fromRangeLabel, 0, 1);

        if (valueLinkingExpression.getSourceValue() != null) {
            NumericConstraint constraint = (NumericConstraint) valueLinkingExpression.getSourceValue().getValue().getConstraint();
            sourceRange = new RangeSliderWithUnit(constraint.getMin(), constraint.getMax(), valueLinkingExpression.getSourceLowValue()
                    , valueLinkingExpression.getSourceHighValue(), valueLinkingExpression.isInverse(), List.of(Unit.NOT_SPECIFIED));
        } else {
            sourceRange = new RangeSliderWithUnit();
        }
        sourceRange.disableProperty().bind(valueCombobox.getSelectionModel().selectedItemProperty().isNull());
        sourceRange.lowValueProperty().addListener((observable, oldValue, newValue) ->
                expression.set(((ValueLinkingExpression) getExpression()).setSourceLowValue(newValue)));
        sourceRange.highValueProperty().addListener((observable, oldValue, newValue) ->
                expression.set(((ValueLinkingExpression) getExpression()).setSourceHighValue(newValue)));
        GridPane.setConstraints(sourceRange, 1, 1, 2, 1);

        Label toLabel = new Label("to range");
        GridPane.setConstraints(toLabel, 0, 2);

        Parameter p = valueLinkingExpression.getDestinationParameter();
        destRange = new RangeSliderWithUnit(p.getMinimumValue(), p.getMaximumValue(), valueLinkingExpression.getDestinationLowValue()
                , valueLinkingExpression.getDestinationHighValue(), false, List.of(Unit.NOT_SPECIFIED));
        destRange.lowValueProperty().addListener((observable, oldValue, newValue) ->
                expression.set(((ValueLinkingExpression) getExpression()).setDestinationLowValue(newValue)));
        destRange.highValueProperty().addListener((observable, oldValue, newValue) ->
                expression.set(((ValueLinkingExpression) getExpression()).setDestinationHighValue(newValue)));
        GridPane.setConstraints(destRange, 1, 2, 2, 1);

        setHgap(10);
        setVgap(5);
        getChildren().addAll(fromLabel, valueSelectionHBox, fromRangeLabel, sourceRange, toLabel, destRange, refreshIntervalHBox, updateLabel);
    }

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
