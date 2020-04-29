/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ComplexStringExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleStringExpression;
import io.makerplayground.project.term.Term;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class StringChipField extends ChipField<ComplexStringExpression> {

    private static final Color NUMERIC_EXPRESSION_COLOR = Color.DARKGRAY;

    public StringChipField(ComplexStringExpression expression, ObservableList<ProjectValue> projectValues) {
        super(expression, projectValues, true, true);
    }

    public StringChipField(ComplexStringExpression expression, ObservableList<ProjectValue> projectValues, boolean allowAnimation) {
        super(expression, projectValues, true, allowAnimation);
    }

    @Override
    ComplexStringExpression convertToExpression(List<Term> terms, boolean hasNonParseText) {
        if (hasNonParseText) {
            return ComplexStringExpression.INVALID;
        } else {
            return new ComplexStringExpression(terms);
        }
    }

    @Override
    protected void updateDisplayMode() {
        displayModeTextFlow.getChildren().clear();
        for (Expression e : getExpression ().getSubExpressions()) {
            Text t;
            if (e instanceof SimpleStringExpression) {
                t = new Text(((SimpleStringExpression) e).getString());
                t.setFill(TEXT_COLOR);
            } else if (e instanceof CustomNumberExpression) {
                t = new Text(e.getTerms().stream().map(Term::toString).collect(Collectors.joining(" ", "(", ")")));
                t.setFill(NUMERIC_EXPRESSION_COLOR);
            } else {
                throw new IllegalStateException();
            }
            displayModeTextFlow.getChildren().add(t);
        }
    }
}
