/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 */
@JsonSerialize (using = ExpressionSerializer.class)
public class Expression {
    private final ObjectProperty<Unit> unit;
    private final ObjectProperty<Operator> operator;
    private final ObjectProperty<Object> firstOperand;
    private final ObjectProperty<Object> secondOperand;
    private final ObjectProperty<OperandType> operandType;

    public Expression() {
        unit = new SimpleObjectProperty<>(Unit.NOT_SPECIFIED);
        operator = new SimpleObjectProperty<>(Operator.GREATER_THAN_LITERAL);
        firstOperand = new SimpleObjectProperty<>(0.0);
        secondOperand = new SimpleObjectProperty<>(0.0);
        operandType = new SimpleObjectProperty<>();
    }

    public Operator getOperator() {
        return operator.get();
    }

    public ObjectProperty<Operator> operatorProperty() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator.set(operator);
    }

    public Object getFirstOperand() {
        return firstOperand.get();
    }

    public ObjectProperty<Object> firstOperandProperty() {
        return firstOperand;
    }

    public void setFirstOperand(Object firstOperand) {
        this.firstOperand.set(firstOperand);
    }

    public Object getSecondOperand() {
        return secondOperand.get();
    }

    public ObjectProperty<Object> secondOperandProperty() {
        return secondOperand;
    }

    public void setSecondOperand(Object secondOperand) {
        this.secondOperand.set(secondOperand);
    }

    public OperandType getOperandType() {
        return operandType.get();
    }

    public ObjectProperty<OperandType> operandTypeProperty() {
        return operandType;
    }

    public void setOperandType(OperandType operandType) {
        this.operandType.set(operandType);
    }

    public Unit getUnit() {
        return unit.get();
    }

    public ObjectProperty<Unit> unitProperty() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit.set(unit);
    }

    @Override
    public String toString() {
        return "Expression{" +
                "unit=" + unit +
                ", operator=" + operator +
                ", firstOperand=" + firstOperand +
                ", secondOperand=" + secondOperand +
                ", operandType=" + operandType +
                '}';
    }
}
