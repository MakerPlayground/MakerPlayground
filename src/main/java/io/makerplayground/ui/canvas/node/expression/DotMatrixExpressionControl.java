package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.DotMatrix;
import io.makerplayground.project.expression.SimpleDotMatrixExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.GridPane;

public class DotMatrixExpressionControl extends GridPane {

    private final ObjectProperty<SimpleDotMatrixExpression> valueProperty;

    public DotMatrixExpressionControl(SimpleDotMatrixExpression expression) {
        valueProperty = new SimpleObjectProperty<>(expression);
        initView();
    }

    private void initView() {
        this.getStylesheets().add("css/canvas/node/expressioncontrol/DotMatrixExpressionControl.css");
        this.setVgap(2);
        this.setHgap(2);
        this.getChildren().clear();
        DotMatrix dotMatrix = valueProperty.get().getByte2D();
        int row = dotMatrix.getRow();
        int column = dotMatrix.getColumn();
        for (int i=0; i<row; i++) {
            for (int j=0; j<column; j++) {
                RadioButton button = new RadioButton();
                button.selectedProperty().set(valueProperty.get().getValueAt(i, j) != 0);
                button.setMaxSize(10, 5);
                final int finalI = i;
                final int finalJ = j;
                button.selectedProperty().addListener((observable, oldValue, newValue) ->
                        valueProperty.get().setValueAt(finalI, finalJ, (byte) (newValue ? 1 : 0)));
                GridPane.setConstraints(button, j, i);
                this.getChildren().add(button);
            }
        }
    }

    public ObjectProperty<SimpleDotMatrixExpression> valueProperty() {
        return valueProperty;
    }
}
