package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.ProjectValueExpression;
import io.makerplayground.project.expression.ValueLinkingExpression;
import io.makerplayground.ui.canvas.node.usersetting.expression.RangeSliderWithUnit;
import io.makerplayground.ui.canvas.node.usersetting.expression.SpinnerWithUnit;
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
    private ChangeListener<Boolean> booleanChangeListener;

    public ValueLinkingControl(Expression expression, List<ProjectValue> projectValues, Parameter parameter) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.projectValues = projectValues;
        this.parameter = parameter;
        this.booleanChangeListener = (observable, oldValue, newValue) -> {
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

        Label fromLabel = new Label("from value");
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
        valueCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(((ProjectValueExpression) expression.get()).setProjectValue(newValue));
        });
        GridPane.setConstraints(valueCombobox, 1, 0);

        CheckBox mappingEnableCheckbox = new CheckBox("map range");
        mappingEnableCheckbox.selectedProperty().addListener(booleanChangeListener);
        GridPane.setConstraints(mappingEnableCheckbox, 2, 0);

        Label refreshIntervalLabel = new Label("refresh interval");
        GridPane.setConstraints(refreshIntervalLabel, 0, 1);

        ComboBox<Expression.RefreshInterval> refreshIntervalComboBox = new ComboBox<>(FXCollections.observableArrayList(Expression.RefreshInterval.values()));
        refreshIntervalComboBox.getSelectionModel().select(getExpression().getRefreshInterval());
        refreshIntervalComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setRefreshInterval(newValue);
        });

        SpinnerWithUnit customIntervalSpinner = new SpinnerWithUnit(0, Double.MAX_VALUE, List.of(Unit.SECOND, Unit.MILLISECOND), getExpression().getUserDefinedInterval());
        customIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setUserDefinedInterval(newValue);
        });
        customIntervalSpinner.visibleProperty().bind(refreshIntervalComboBox.getSelectionModel().selectedItemProperty().isEqualTo(Expression.RefreshInterval.USER_DEFINED));
        customIntervalSpinner.managedProperty().bind(customIntervalSpinner.visibleProperty());

        HBox refreshIntervalHBox = new HBox(5);
        refreshIntervalHBox.getChildren().addAll(refreshIntervalComboBox, customIntervalSpinner);
        GridPane.setConstraints(refreshIntervalHBox, 1, 1, 2, 1);

        setHgap(10);
        setVgap(5);
        getChildren().addAll(fromLabel, valueCombobox, mappingEnableCheckbox, refreshIntervalLabel, refreshIntervalHBox);
    }

    private void initValueLinkingControl() {
        getChildren().clear();

        ValueLinkingExpression valueLinkingExpression = (ValueLinkingExpression) getExpression();

        Label fromLabel = new Label("from value");
        GridPane.setConstraints(fromLabel, 0, 0);

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
        GridPane.setConstraints(valueCombobox, 1, 0);

        CheckBox mappingEnableCheckbox = new CheckBox("map range");
        mappingEnableCheckbox.setSelected(true);
        mappingEnableCheckbox.selectedProperty().addListener(booleanChangeListener);
        GridPane.setConstraints(mappingEnableCheckbox, 2, 0);

        Label fromRangeLabel = new Label("from range");
        GridPane.setConstraints(fromRangeLabel, 0, 1);

        sourceRange = new RangeSliderWithUnit();
        sourceRange.disableProperty().bind(valueCombobox.getSelectionModel().selectedItemProperty().isNull());
        if (valueLinkingExpression.getSourceValue() != null) {
            NumericConstraint constraint = (NumericConstraint) valueLinkingExpression.getSourceValue().getValue().getConstraint();
            sourceRange.setMaxValue(new NumberWithUnit(constraint.getMax(), constraint.getUnit()));
            sourceRange.setMinValue(new NumberWithUnit(constraint.getMin(), constraint.getUnit()));
            sourceRange.setHighValue(valueLinkingExpression.getSourceHighValue());
            sourceRange.setLowValue(valueLinkingExpression.getSourceLowValue());
        }
        sourceRange.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(((ValueLinkingExpression) getExpression()).setSourceLowValue(newValue));
        });
        sourceRange.highValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(((ValueLinkingExpression) getExpression()).setSourceHighValue(newValue));
        });
        GridPane.setConstraints(sourceRange, 1, 1, 2, 1);

        Label toLabel = new Label("to range");
        GridPane.setConstraints(toLabel, 0, 2);

        destRange = new RangeSliderWithUnit();
        Parameter p = valueLinkingExpression.getDestinationParameter();
        destRange.setMaxValue(new NumberWithUnit(p.getMaximumValue(), p.getUnit().get(0)));
        destRange.setMinValue(new NumberWithUnit(p.getMinimumValue(), p.getUnit().get(0)));
        destRange.setHighValue(valueLinkingExpression.getDestinationHighValue());
        destRange.setLowValue(valueLinkingExpression.getDestinationLowValue());
        destRange.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(((ValueLinkingExpression) getExpression()).setDestinationLowValue(newValue));
        });
        destRange.highValueProperty().addListener((observable, oldValue, newValue) -> {
            expression.set(((ValueLinkingExpression) getExpression()).setDestinationHighValue(newValue));
        });
        GridPane.setConstraints(destRange, 1, 2, 2, 1);

        Label refreshIntervalLabel = new Label("refresh interval");
        GridPane.setConstraints(refreshIntervalLabel, 0, 3);

        ComboBox<Expression.RefreshInterval> refreshIntervalComboBox = new ComboBox<>(FXCollections.observableArrayList(Expression.RefreshInterval.values()));
        refreshIntervalComboBox.getSelectionModel().select(getExpression().getRefreshInterval());
        refreshIntervalComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setRefreshInterval(newValue);
        });

        SpinnerWithUnit customIntervalSpinner = new SpinnerWithUnit(0, Double.MAX_VALUE, List.of(Unit.SECOND, Unit.MILLISECOND), getExpression().getUserDefinedInterval());
        customIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            getExpression().setUserDefinedInterval(newValue);
        });
        customIntervalSpinner.visibleProperty().bind(refreshIntervalComboBox.getSelectionModel().selectedItemProperty().isEqualTo(Expression.RefreshInterval.USER_DEFINED));
        customIntervalSpinner.managedProperty().bind(customIntervalSpinner.visibleProperty());

        HBox refreshIntervalHBox = new HBox(5);
        refreshIntervalHBox.getChildren().addAll(refreshIntervalComboBox, customIntervalSpinner);
        GridPane.setConstraints(refreshIntervalHBox, 1, 3, 2, 1);

        setHgap(10);
        setVgap(5);
        getChildren().addAll(fromLabel, valueCombobox, mappingEnableCheckbox, fromRangeLabel, sourceRange, toLabel, destRange, refreshIntervalLabel, refreshIntervalHBox);
    }

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
