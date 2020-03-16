/*
 * Copyright (c) 2020. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.term.StringTerm;

public class StringIntegerExpression extends Expression {

    private final StringIntegerCategoricalConstraint constraint;

    public StringIntegerExpression(StringIntegerCategoricalConstraint constraint, String value) {
        super(Type.STRING_INT);
        this.constraint = constraint;
        this.terms.add(new StringTerm(value));
    }

    public StringIntegerExpression(StringIntegerExpression expression) {
        super(expression);
        constraint = expression.constraint;
    }

    @Override
    public Expression deepCopy() {
        return new StringIntegerExpression(this);
    }

    @JsonIgnore
    public String getString() {
        return ((StringTerm) terms.get(0)).getValue();
    }

    @JsonIgnore
    public int getInteger() {
        return constraint.getMap().get(getString());
    }
}