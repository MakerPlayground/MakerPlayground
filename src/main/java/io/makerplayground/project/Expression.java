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

import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;

/**
 *
 */
public class Expression {
    private Operator operator;
    private Object firstOperand;
    private Object secondOperand;
    private OperandType operandType;

    public Expression() {

    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getFirstOperand() {
        return firstOperand;
    }

    public void setFirstOperand(Object firstOperand) {
        this.firstOperand = firstOperand;
    }

    public Object getSecondOperand() {
        return secondOperand;
    }

    public void setSecondOperand(Object secondOperand) {
        this.secondOperand = secondOperand;
    }

    public OperandType getOperandType() {
        return operandType;
    }

    public void setOperandType(OperandType operandType) {
        this.operandType = operandType;
    }
}
