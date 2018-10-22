package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.Value;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

public class ConditionalExpressionControl extends HBox {
    private final ProjectDevice projectDevice;
    private final Value value;
    private final List<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<Expression> expression = new ReadOnlyObjectWrapper<>();

    public ConditionalExpressionControl(ProjectDevice projectDevice, Value v, List<ProjectValue> projectValues, Expression expression) {
        this.projectDevice = projectDevice;
        this.value = v;
        this.projectValues = projectValues;
        this.expression.set(expression);
        initView();
    }

    private void initView() {
        getChildren().clear();

        RadioMenuItem simpleRadioButton = new RadioMenuItem("Simple");
        RadioMenuItem customRadioButton = new RadioMenuItem("Custom");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(simpleRadioButton, customRadioButton);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(simpleRadioButton, customRadioButton);

        ImageView configButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/advance-setting-press.png")));
        configButton.setFitWidth(25);
        configButton.setPreserveRatio(true);
        configButton.setOnMousePressed(event -> contextMenu.show(configButton, Side.BOTTOM, 0, 0));

        Node control;
        if (getExpression() instanceof NumberInRangeExpression) {
            SimpleConditionalExpressionControl expressionControl = new SimpleConditionalExpressionControl((NumberInRangeExpression) getExpression(), value);
            expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
            toggleGroup.selectToggle(simpleRadioButton);
            control = expressionControl;
        } else if (getExpression() instanceof ConditionalExpression) {
            CustomConditionalExpressionControl expressionControl = new CustomConditionalExpressionControl((ConditionalExpression) getExpression(), projectValues);
            expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
            toggleGroup.selectToggle(customRadioButton);
            control = expressionControl;
        } else {
            throw new IllegalStateException("Found unsupported expression!!!");
        }

        getChildren().addAll(control, configButton);
        setSpacing(5);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == simpleRadioButton) {
                expression.set(new NumberInRangeExpression(projectDevice, value));
            } else if (newValue == customRadioButton) {
                expression.set(new ConditionalExpression(projectDevice, value));
            }
            initView();
        });
    }

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
