package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.expression.Expression;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DoubleNumberExpressionControl extends VBox {

    private Node mainControl;
    private CheckBox advanceCheckBox;
    private Expression expression;

    private DoubleProperty minimumProperty;
    private DoubleProperty maximumProperty;
    private ObjectProperty<NumberWithUnit> numberWithUnitProperty;
    private ListProperty<Unit> unitListProperty;
    private final SplitPane splitPane;

    public DoubleNumberExpressionControl(double minimumValue, double maximumValue, ObservableList<Unit> units, NumberWithUnit number) {
        this.minimumProperty = new SimpleDoubleProperty(minimumValue);
        this.maximumProperty = new SimpleDoubleProperty(maximumValue);
        this.unitListProperty = new SimpleListProperty<>(units);
        this.numberWithUnitProperty = new SimpleObjectProperty<>(number);
        this.advanceCheckBox = new CheckBox("Advanced");
        this.advanceCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::redrawControl));
        this.mainControl = new SliderWithUnit(minimumProperty.get(), maximumProperty.get(), unitListProperty.get(), numberWithUnitProperty.get());
        this.redrawControl();

        FlowPane operandChipPane = new FlowPane();
        operandChipPane.setPrefSize(100, 150);
        operandChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        FlowPane operatorChipPane = new FlowPane();
        operatorChipPane.setPrefSize(100, 150);
        operatorChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        for (int i=0; i<20; i++) {
            Rectangle rectangle = new Rectangle();
            rectangle.setHeight(20);
            rectangle.setWidth(40);
            operandChipPane.getChildren().addAll(rectangle);
            operatorChipPane.getChildren().addAll(rectangle);
        }

        splitPane = new SplitPane(operandChipPane, operatorChipPane);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.5);
        this.setOnMouseEntered(event -> {
            if (advanceCheckBox.selectedProperty().get()) {
                getChildren().add(splitPane);
            }
        });
        this.setOnMouseExited(event -> {
            getChildren().remove(splitPane);
        });
    }

    private void redrawControl() {
        this.getChildren().clear();
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5.0);
        if (advanceCheckBox.selectedProperty().get()) {
            FlowPane pane = new FlowPane(Orientation.HORIZONTAL, 3.0, 2.0);
            pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            pane.setPrefSize(300, 20);
            for (int i=0; i<5; i++) {
                Rectangle rectangle = new Rectangle();
                rectangle.setHeight(20);
                rectangle.setWidth(40);
                pane.getChildren().addAll(rectangle);
            }
            mainControl = pane;
        } else {
            mainControl = new SliderWithUnit(minimumProperty.get(), maximumProperty.get(), unitListProperty.get(), numberWithUnitProperty.get());
        }
        hbox.getChildren().addAll(mainControl, advanceCheckBox);
        getChildren().addAll(hbox);
        if (advanceCheckBox.selectedProperty().get()) {
            getChildren().add(splitPane);
        }
    }

    public Expression getExpression() {
        return expression;
    }

    public ObjectProperty<NumberWithUnit> valueProperty() {
        return numberWithUnitProperty;
    }
}
