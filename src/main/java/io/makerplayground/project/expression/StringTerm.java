package io.makerplayground.project.expression;

public class StringTerm extends Term {
    public StringTerm(String value) {
        super(ChipType.STRING, value);
    }

    @Override
    public String getValue() {
        return (String) value;
    }
}
