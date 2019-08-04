package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ComplexStringExpression;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleStringExpression;
import io.makerplayground.project.term.Term;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class StringChipField extends ChipField<ComplexStringExpression> {

    private static final Color NUMERIC_EXPRESSION_COLOR = Color.DARKGRAY;

    public StringChipField(ComplexStringExpression expression, List<ProjectValue> projectValues) {
        super(expression, projectValues, true);
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
