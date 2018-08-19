package io.makerplayground.project.term;

public class OperatorTerm extends Term {

    public OperatorTerm(Operator value) {
        super(Term.Type.OPERATOR, value);
    }

    @Override
    public Operator getValue() {
        return (Operator) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toCCode() {
        return getValue().getCodeString();
    }

}


