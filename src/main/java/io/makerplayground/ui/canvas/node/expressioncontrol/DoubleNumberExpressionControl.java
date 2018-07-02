package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberWithUnitExpression;
import io.makerplayground.ui.canvas.chip.ChipField;
import io.makerplayground.ui.canvas.node.usersetting.NumberWithUnitPopOver;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

public class DoubleNumberExpressionControl extends VBox {

    private Node mainControl;
    private CheckBox advanceCheckBox;

    private DoubleProperty minimumProperty;
    private DoubleProperty maximumProperty;
    private ListProperty<Unit> unitListProperty;

    private ObjectProperty<Expression> expressionProperty = new SimpleObjectProperty<>();

    private NumberWithUnitPopOver popOver;
    private final ObservableList<ProjectValue> projectValues;

    public DoubleNumberExpressionControl(double minimumValue,
                                         double maximumValue,
                                         ObservableList<Unit> units,
                                         ObservableList<ProjectValue> projectValues,
                                         Expression expression) {
        this.minimumProperty = new SimpleDoubleProperty(minimumValue);
        this.maximumProperty = new SimpleDoubleProperty(maximumValue);
        this.unitListProperty = new SimpleListProperty<>(units);
        this.advanceCheckBox = new CheckBox("Advanced");
        this.advanceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::redrawControl));

        this.projectValues = projectValues;
        this.expressionProperty.set(expression);
        this.advanceCheckBox.selectedProperty().set(expression instanceof CustomNumberExpression);

        this.advanceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                expressionProperty.set(new CustomNumberExpression());
            } else {
                expressionProperty.set(new NumberWithUnitExpression(new NumberWithUnit(0.0, Unit.NOT_SPECIFIED)));
            }
        });

        this.redrawControl();
    }

    private void redrawControl() {
        this.getChildren().clear();
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5.0);
        if (advanceCheckBox.selectedProperty().get()) {
            ChipField chipField = new ChipField((CustomNumberExpression) expressionProperty.get(), projectValues);
            chipField.setOnMousePressed(event -> {
                if (popOver != null) {
                    popOver.hide();
                }
                popOver = new NumberWithUnitPopOver((ChipField) mainControl);
                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
                popOver.show(mainControl);
            });
            this.expressionProperty.bind(chipField.expressionProperty());

            mainControl = chipField;

        } else {
            NumberWithUnit numberWithUnit;
            numberWithUnit = (NumberWithUnit) expressionProperty.get().getTerms().get(0).getValue();
            mainControl = new SliderWithUnit(minimumProperty.get(), maximumProperty.get(), unitListProperty.get(), numberWithUnit);
        }
        hbox.getChildren().addAll(mainControl, advanceCheckBox);
        getChildren().addAll(hbox);
    }

    public ObjectProperty<Expression> expressionProperty() {
        return expressionProperty;
    }

}
