package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

public class MultiFunctionNumericControl extends HBox {

    private final ReadOnlyObjectWrapper<CustomNumberExpression> expression;

    public MultiFunctionNumericControl(Parameter p, List<ProjectValue> projectValues, CustomNumberExpression expression) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);

        NumericChipField chipField = new NumericChipField(expression, projectValues);
        chipField.expressionProperty().addListener((observable, oldValue, newValue) -> this.expression.set(newValue));
        getChildren().add(chipField);

        if (!p.getUnit().isEmpty() && p.getUnit().get(0) != Unit.NOT_SPECIFIED) {
            Label unitLabel = new Label(p.getUnit().get(0).toString());
            unitLabel.setMinHeight(27);
            getChildren().add(unitLabel);
        }

        Label rangeLabel = new Label("(" + p.getMinimumValue() + "-" + p.getMaximumValue() + ")");
        rangeLabel.setMinHeight(27);
        getChildren().add(rangeLabel);

        setAlignment(Pos.TOP_LEFT);
        setSpacing(5);
    }

    public CustomNumberExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
