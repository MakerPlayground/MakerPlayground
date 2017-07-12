package io.makerplayground.ui.canvas;

import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.Expression;
import io.makerplayground.project.ProjectValue;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

import java.util.List;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionViewModel {
    private final Expression expression;
    private final List<ProjectValue> availableValue;

    private final DoubleProperty firstOperandAsDouble;
    private final DoubleProperty secondOperandAsDouble;
    private final ObjectProperty<ProjectValue> firstOperandAsValue;
    private final ObjectProperty<ProjectValue> secondOperandAsValue;
    private final ReadOnlyBooleanWrapper literalMode;
    private final ReadOnlyBooleanWrapper betweenMode;

    public ExpressionViewModel(Expression expression, List<ProjectValue> values) {
        this.expression = expression;
        this.availableValue = values;

        this.firstOperandAsDouble = new SimpleDoubleProperty();
        this.firstOperandAsDouble.addListener((observable, oldValue, newValue) -> expression.setFirstOperand(newValue));
        this.secondOperandAsDouble = new SimpleDoubleProperty();
        this.secondOperandAsDouble.addListener((observable, oldValue, newValue) -> expression.setSecondOperand(newValue));
        this.firstOperandAsValue = new SimpleObjectProperty<>();
        this.firstOperandAsValue.addListener((observable, oldValue, newValue) -> expression.setFirstOperand(newValue));
        this.secondOperandAsValue = new SimpleObjectProperty<>();
        this.secondOperandAsValue.addListener((observable, oldValue, newValue) -> expression.setSecondOperand(newValue));
        this.literalMode = new ReadOnlyBooleanWrapper();
        this.literalMode.bind(Bindings.createBooleanBinding(() -> expression.getOperator().isLiteral(), expression.operatorProperty()));
        this.betweenMode = new ReadOnlyBooleanWrapper();
        this.betweenMode.bind(Bindings.createBooleanBinding(() -> expression.getOperator().isBetween(), expression.operatorProperty()));

        if (expression.getOperator().isLiteral()) {
            this.firstOperandAsDouble.set((Double) expression.getFirstOperand());

            if (expression.getOperator().isBetween()) {
                this.secondOperandAsDouble.set((Double) expression.getSecondOperand());
            }
        } else if (expression.getOperator().isVariable()) {
            this.firstOperandAsValue.set((ProjectValue) expression.getFirstOperand());

            if (expression.getOperator().isBetween()) {
                this.secondOperandAsValue.set((ProjectValue) expression.getSecondOperand());
            }
        }

        expression.operatorProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isLiteral()) {
                expression.setFirstOperand(firstOperandAsDouble.get());
                if (newValue.isBetween()) {
                    expression.setSecondOperand(secondOperandAsDouble.get());
                }
            } else if (newValue.isVariable()) {
                expression.setFirstOperand(firstOperandAsValue.get());
                if (newValue.isBetween()) {
                    expression.setSecondOperand(secondOperandAsValue.get());
                }
            }
        });
    }

    public double getFirstOperandAsDouble() {
        return firstOperandAsDouble.get();
    }

    public DoubleProperty firstOperandAsDoubleProperty() {
        return firstOperandAsDouble;
    }

    public void setFirstOperandAsDouble(double firstOperandAsDouble) {
        this.firstOperandAsDouble.set(firstOperandAsDouble);
    }

    public double getSecondOperandAsDouble() {
        return secondOperandAsDouble.get();
    }

    public DoubleProperty secondOperandAsDoubleProperty() {
        return secondOperandAsDouble;
    }

    public void setSecondOperandAsDouble(double secondOperandAsDouble) {
        this.secondOperandAsDouble.set(secondOperandAsDouble);
    }

    public ProjectValue getFirstOperandAsValue() {
        return firstOperandAsValue.get();
    }

    public ObjectProperty<ProjectValue> firstOperandAsValueProperty() {
        return firstOperandAsValue;
    }

    public void setFirstOperandAsValue(ProjectValue firstOperandAsValue) {
        this.firstOperandAsValue.set(firstOperandAsValue);
    }

    public ProjectValue getSecondOperandAsValue() {
        return secondOperandAsValue.get();
    }

    public ObjectProperty<ProjectValue> secondOperandAsValueProperty() {
        return secondOperandAsValue;
    }

    public void setSecondOperandAsValue(ProjectValue secondOperandAsValue) {
        this.secondOperandAsValue.set(secondOperandAsValue);
    }

    public Operator getOperator() { return expression.getOperator(); }

    public ObjectProperty<Operator> operatorProperty() {
        return expression.operatorProperty();
    }

    public Expression getExpression() {
        return expression;
    }

    public Unit getUnit() { return expression.getUnit(); }

    public ObjectProperty<Unit> unitProperty() { return expression.unitProperty(); }

    public List<ProjectValue> getAvailableValue() {
        return availableValue;
    }

    public boolean isLiteralMode() {
        return literalMode.get();
    }

    public ReadOnlyBooleanProperty literalModeProperty() {
        return literalMode.getReadOnlyProperty();
    }

    public boolean isBetweenMode() {
        return betweenMode.get();
    }

    public ReadOnlyBooleanProperty betweenModeProperty() {
        return betweenMode.getReadOnlyProperty();
    }
}
