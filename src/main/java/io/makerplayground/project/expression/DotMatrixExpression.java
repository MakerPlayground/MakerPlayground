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
import io.makerplayground.device.shared.RGBDotMatrix;
import io.makerplayground.device.shared.SingleColorDotMatrix;
import io.makerplayground.project.term.RGBDotMatrixTerm;
import io.makerplayground.project.term.SingleColorDotMatrixTerm;

public class DotMatrixExpression extends Expression {

    public DotMatrixExpression(DotMatrix dotMatrix) {
        super(Type.DOT_MATRIX);
        if (dotMatrix instanceof SingleColorDotMatrix) {
            terms.add(new SingleColorDotMatrixTerm((SingleColorDotMatrix) dotMatrix));
        } else if (dotMatrix instanceof RGBDotMatrix) {
            terms.add(new RGBDotMatrixTerm((RGBDotMatrix) dotMatrix));
        } else {
            throw new IllegalStateException();
        }
    }

    @JsonIgnore
    public DotMatrix getDotMatrix() {
        return (DotMatrix) getTerms().get(0).getValue();
    }

    @Override
    public DotMatrixExpression deepCopy() {
        DotMatrix dotMatrix = getDotMatrix();
        if (dotMatrix instanceof SingleColorDotMatrix) {
            return new DotMatrixExpression(new SingleColorDotMatrix(dotMatrix.getRow(), dotMatrix.getColumn(), dotMatrix.getDataAsString()));
        } else if (getDotMatrix() instanceof RGBDotMatrix) {
            return new DotMatrixExpression(new RGBDotMatrix(dotMatrix.getRow(), dotMatrix.getColumn(), dotMatrix.getDataAsString()));
        } else {
            throw new IllegalStateException();
        }

    }
}
