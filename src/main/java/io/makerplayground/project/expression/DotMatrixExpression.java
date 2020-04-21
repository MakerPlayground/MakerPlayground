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
import io.makerplayground.device.shared.DotMatrix;
import io.makerplayground.project.term.DotMatrixTerm;

public class DotMatrixExpression extends Expression {

    public DotMatrixExpression(DotMatrix dotMatrix) {
        super(Type.DOT_MATRIX);
        terms.add(new DotMatrixTerm(dotMatrix));
    }

    @JsonIgnore
    public DotMatrix getDotMatrix() {
        return (DotMatrix) getTerms().get(0).getValue();
    }

    public DotMatrixExpression(DotMatrixExpression dotMatrixExpression) {
        super(dotMatrixExpression);
    }

    @Override
    public DotMatrixExpression deepCopy() {
        return new DotMatrixExpression(this);
    }
}
