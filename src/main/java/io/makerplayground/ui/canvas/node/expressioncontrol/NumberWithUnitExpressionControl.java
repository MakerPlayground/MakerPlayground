package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberWithUnitExpression;
import io.makerplayground.project.expression.ValueLinkingExpression;
import io.makerplayground.ui.canvas.node.usersetting.ValueLinkingControl;
import io.makerplayground.ui.canvas.node.usersetting.chip.ChipField;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public abstract class NumberWithUnitExpressionControl extends HBox {

    private final Parameter parameter;
    private final List<ProjectValue> projectValues;

    private ReadOnlyObjectWrapper<Expression> expression = new ReadOnlyObjectWrapper<>();

    public NumberWithUnitExpressionControl(Parameter p, List<ProjectValue> projectValues, Expression expression) {
        this.parameter = p;
        this.projectValues = projectValues;
        this.expression.set(expression);
        initView();
    }

    private void initView() {
        getChildren().clear();

        RadioMenuItem numberRadioButton = new RadioMenuItem("Number");
        RadioMenuItem valueRadioButton = new RadioMenuItem("Value");
        RadioMenuItem customRadioButton = new RadioMenuItem("Custom");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(numberRadioButton, valueRadioButton, customRadioButton);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(numberRadioButton, valueRadioButton, customRadioButton);

        Button configButton = new Button("Gear");
        configButton.setOnAction(event -> contextMenu.show(configButton, Side.BOTTOM, 0, 0));

        if (getExpression() instanceof CustomNumberExpression) {
            ChipField chipField = new ChipField((CustomNumberExpression) getExpression(), projectValues);
            chipField.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
            toggleGroup.selectToggle(customRadioButton);
            getChildren().add(chipField);
        } else if (getExpression() instanceof NumberWithUnitExpression) {
            NumberWithUnit numberWithUnit = ((NumberWithUnitExpression) getExpression()).getNumberWithUnit();
            NumberWithUnitControl numberWithUnitControl = createNumberWithUnitControl(parameter.getMinimumValue(), parameter.getMaximumValue(), parameter.getUnit(), numberWithUnit);
            numberWithUnitControl.valueProperty().addListener((observable, oldValue, newValue) -> expression.set(new NumberWithUnitExpression(newValue)));
            toggleGroup.selectToggle(numberRadioButton);
            getChildren().add(numberWithUnitControl);
        } else if (getExpression() instanceof ValueLinkingExpression) {
            ValueLinkingControl valueLinkingControl = new ValueLinkingControl((ValueLinkingExpression) getExpression(), projectValues);
            valueLinkingControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
            toggleGroup.selectToggle(valueRadioButton);
            getChildren().add(valueLinkingControl);
        } else {
            throw new IllegalStateException();
        }

        getChildren().add(configButton);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == numberRadioButton) {
                expression.set(new NumberWithUnitExpression(new NumberWithUnit(parameter.getMinimumValue(), parameter.getUnit().get(0))));
            } else if (newValue == valueRadioButton) {
                expression.set(new ValueLinkingExpression(parameter));
            } else if (newValue == customRadioButton) {
                expression.set(new CustomNumberExpression(parameter.getMinimumValue(), parameter.getMaximumValue()));
            }
            initView();
        });
    }

    protected abstract NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue);

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
