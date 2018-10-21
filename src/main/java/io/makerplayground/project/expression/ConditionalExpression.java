package io.makerplayground.project.expression;

import io.makerplayground.device.shared.Value;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.*;

import java.util.List;

public class ConditionalExpression extends Expression {

    public static final List<Operator> OPERATORS = List.of(Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL
            , Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.EQUAL, Operator.NOT_EQUAL
            , Operator.BETWEEN, Operator.NOT_BETWEEN);

    private final ProjectDevice projectDevice;
    private final Value value;

    public ConditionalExpression(ProjectDevice projectDevice, Value value) {
        super(Type.CONDITIONAL);
        this.projectDevice = projectDevice;
        this.value = value;
    }

    public ConditionalExpression(ProjectDevice projectDevice, Value value, List<Term> terms) {
        super(Type.CONDITIONAL);
        this.projectDevice = projectDevice;
        this.value = value;
        this.terms.addAll(terms);

        int i = 0;
        while (i < terms.size()) {
            Term t = terms.get(i++);
            if (!(t instanceof OperatorTerm)) {
                throw new IllegalStateException();
            }
            Operator operator = ((OperatorTerm) t).getValue();
            t = terms.get(i++);
            if (!(t instanceof NumberWithUnitTerm || t instanceof ValueTerm)) {
                throw new IllegalStateException();
            }
            if (operator == Operator.BETWEEN || operator == Operator.NOT_BETWEEN) {
                t = terms.get(i++);
                if (!(t instanceof NumberWithUnitTerm || t instanceof ValueTerm)) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    ConditionalExpression(ConditionalExpression e) {
        super(e);
        this.projectDevice = e.projectDevice;
        this.value = e.value;
    }

    public ProjectDevice getProjectDevice() {
        return projectDevice;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String translateToCCode() {
        StringBuilder sb = new StringBuilder();
        ValueTerm valueTerm = new ValueTerm(new ProjectValue(projectDevice, value));

        int i = 0;
        while (i < terms.size()) {
            if (i != 0) {
                sb.append(" && ");
            }

            Operator operator = ((OperatorTerm) terms.get(i++)).getValue();
            Term lowTerm = terms.get(i++);
            Term highTerm;
            if (operator == Operator.BETWEEN) {
                highTerm = terms.get(i++);
                sb.append("(").append(valueTerm.toCCode()).append(">=").append(lowTerm.toCCode()).append(") && (")
                        .append(valueTerm.toCCode()).append("<=").append(highTerm.toCCode()).append(")");
            } else if (operator == Operator.NOT_BETWEEN) {
                highTerm = terms.get(i++);
                sb.append("(").append(valueTerm.toCCode()).append("<").append(lowTerm.toCCode()).append(") && (")
                        .append(valueTerm.toCCode()).append(">").append(highTerm.toCCode()).append(")");
            } else {
                sb.append("(").append(valueTerm.toCCode()).append(operator.getCodeString()).append(lowTerm.toCCode()).append(")");
            }
        }

        return sb.toString();
    }
}
