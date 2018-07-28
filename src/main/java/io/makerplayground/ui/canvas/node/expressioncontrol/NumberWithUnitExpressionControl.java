package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberWithUnitExpression;
import io.makerplayground.ui.canvas.node.usersetting.NumberWithUnitPopOver;
import io.makerplayground.ui.canvas.node.usersetting.chip.ChipField;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.List;

public abstract class NumberWithUnitExpressionControl extends VBox {

    private Node mainControl;
    private CheckBox advanceCheckBox;

    private double maxValue;
    private double minValue;
    private List<Unit> unitList;

    private ObjectProperty<CustomNumberExpression> customNumberExpressionProperty = new SimpleObjectProperty<>();
    private ObjectProperty<NumberWithUnitExpression> numberWithUnitExpressionProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Expression> expressionProperty = new SimpleObjectProperty<>();

    private NumberWithUnitPopOver popOver;
    private final List<ProjectValue> projectValues;

    public NumberWithUnitExpressionControl(double minimumValue,
                                           double maximumValue,
                                           List<Unit> units,
                                           List<ProjectValue> projectValues,
                                           Expression expression) {
        this.maxValue = maximumValue;
        this.minValue = minimumValue;
        this.unitList = units;
        this.advanceCheckBox = new CheckBox("Advanced");

        this.projectValues = projectValues;
        if (expression instanceof CustomNumberExpression) {
            this.advanceCheckBox.selectedProperty().set(true);
            this.customNumberExpressionProperty.set((CustomNumberExpression) expression);
            this.numberWithUnitExpressionProperty.set(new NumberWithUnitExpression(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED)));
            this.expressionProperty.bind(this.customNumberExpressionProperty);
        } else if (expression instanceof NumberWithUnitExpression) {
            this.advanceCheckBox.selectedProperty().set(false);
            this.customNumberExpressionProperty.set(new CustomNumberExpression(maximumValue, minimumValue));
            this.numberWithUnitExpressionProperty.set((NumberWithUnitExpression) expression);
            this.expressionProperty.bind(this.numberWithUnitExpressionProperty);
        } else {
            this.advanceCheckBox.selectedProperty().set(false);
            this.customNumberExpressionProperty.set(new CustomNumberExpression(maximumValue, minimumValue));
            this.numberWithUnitExpressionProperty.set(new NumberWithUnitExpression(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED)));
            this.expressionProperty.bind(this.numberWithUnitExpressionProperty);
        }

        this.advanceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.expressionProperty.unbind();
            if (newValue) {
                this.expressionProperty.bind(this.customNumberExpressionProperty);
            } else {
                this.expressionProperty.bind(this.numberWithUnitExpressionProperty);
            }
            Platform.runLater(this::redrawControl);
        });
        this.redrawControl();
    }

    private void redrawControl() {
        this.getChildren().clear();
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5.0);
        if (advanceCheckBox.selectedProperty().get()) {
            ChipField chipField = new ChipField(customNumberExpressionProperty, projectValues);
            chipField.setOnMousePressed(event -> {
                if (popOver != null) {
                    popOver.hide();
                }
                popOver = new NumberWithUnitPopOver((ChipField) mainControl);
                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
                popOver.show(mainControl);
            });
            mainControl = chipField;
        } else {
            NumberWithUnit numberWithUnit = (NumberWithUnit) numberWithUnitExpressionProperty.get().getTerms().get(0).getValue();
            NumberWithUnitControl numberWithUnitControl = createNumberWithUnitControl(minValue, maxValue, unitList, numberWithUnit);
            numberWithUnitControl.valueProperty().addListener((observable, oldValue, newValue) -> numberWithUnitExpressionProperty.setValue(new NumberWithUnitExpression(newValue)));
            mainControl = numberWithUnitControl;
        }
        hbox.getChildren().addAll(mainControl, advanceCheckBox);
        getChildren().addAll(hbox);
    }

    protected abstract NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue);

    public ObjectProperty<Expression> expressionProperty() {
        return expressionProperty;
    }

}
