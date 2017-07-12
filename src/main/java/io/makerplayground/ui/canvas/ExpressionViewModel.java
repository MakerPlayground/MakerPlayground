package io.makerplayground.ui.canvas;

import io.makerplayground.device.Value;
import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.Expression;
import io.makerplayground.project.Project;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.util.HashMap;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionViewModel {
    private final Expression expression;
    private final ObservableList<Value> availableValue;

    public ExpressionViewModel(Expression expression, ObservableList<Value> values) {
        this.expression = expression;
        this.availableValue = values;
    }

    public ObjectProperty<Operator> operatorProperty() {
        return expression.operatorProperty();
    }

    public ObjectProperty<Object> firstOperandProperty() {
        return expression.firstOperandProperty();
    }

    public ObjectProperty<Object> secondOperandProperty() {
        return expression.secondOperandProperty();
    }

    public ObjectProperty<OperandType> operandTypeProperty() {
        return expression.operandTypeProperty();
    }

    public Operator getOperator() { return expression.getOperator(); }

    public Expression getExpression() {
        return expression;
    }

    public Unit getUnit() { return expression.getUnit(); }

    public ObjectProperty<Unit> unitProperty() { return expression.unitProperty(); }

}
