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

import static io.makerplayground.project.expression.Expression.Type.CUSTOM_NUMBER;

public class DoubleNumberExpressionControl extends VBox {

    private Node mainControl;
    private CheckBox advanceCheckBox;
    private Expression expression;

    private DoubleProperty minimumProperty;
    private DoubleProperty maximumProperty;
    private ObjectProperty<NumberWithUnit> numberWithUnitProperty;
    private ListProperty<Unit> unitListProperty;
//    private final SplitPane splitPane;
    private NumberWithUnitPopOver popOver;
    private final ObservableList<ProjectValue> projectValues;

    public DoubleNumberExpressionControl(double minimumValue, double maximumValue, ObservableList<Unit> units, NumberWithUnit number, ObservableList<ProjectValue> projectValues, Expression expression) {
        this.minimumProperty = new SimpleDoubleProperty(minimumValue);
        this.maximumProperty = new SimpleDoubleProperty(maximumValue);
        this.unitListProperty = new SimpleListProperty<>(units);
        this.numberWithUnitProperty = new SimpleObjectProperty<>(number);
        this.advanceCheckBox = new CheckBox("Advanced");
        this.advanceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::redrawControl));
        this.mainControl = new SliderWithUnit(minimumProperty.get(), maximumProperty.get(), unitListProperty.get(), numberWithUnitProperty.get());
        this.projectValues = projectValues;
        if (expression == null) {
            this.expression = new Expression(CUSTOM_NUMBER);
        } else {
            this.expression = expression;
        }

        this.redrawControl();


//        this.setOnMouseReleased(event -> {
//            if (advanceCheckBox.selectedProperty().get()) {
//                if(popOver != null) {
//                    popOver.hide();
//                }
//            }
//        });
    }

    private void redrawControl() {
        this.getChildren().clear();
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5.0);
        if (advanceCheckBox.selectedProperty().get()) {
            if (expression instanceof NumberWithUnitExpression) {
                expression = new CustomNumberExpression();
            }
            ChipField chipField = new ChipField(expression, projectValues);
            chipField.setOnMousePressed(event -> {
                if (popOver != null) {
                    popOver.hide();
                    popOver = null;
                }
                popOver = new NumberWithUnitPopOver((ChipField) mainControl);
                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
                popOver.show(mainControl);
            });
            mainControl = chipField;

        } else {
            mainControl = new SliderWithUnit(minimumProperty.get(), maximumProperty.get(), unitListProperty.get(), numberWithUnitProperty.get());
        }
        hbox.getChildren().addAll(mainControl, advanceCheckBox);
        getChildren().addAll(hbox);
//        if (advanceCheckBox.selectedProperty().get()) {
//            popOver = new NumberWithUnitPopOver();
//            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
//            popOver.show(this);
//        }
    }

    public Expression getExpression() {
        return expression;
    }

    public ObjectProperty<NumberWithUnit> valueProperty() {
        return numberWithUnitProperty;
    }
}
