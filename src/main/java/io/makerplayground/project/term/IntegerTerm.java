package io.makerplayground.project.term;

public class IntegerTerm extends Term {
    public IntegerTerm(Integer value) {
        super(Type.NUMBER_ONLY, value);
    }

    @Override
    public Integer getValue() {
        return (Integer) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
