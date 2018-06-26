package io.makerplayground.project.chip;

public class StringTerm extends Term {
    public StringTerm(String value) {
        super(Type.STRING, value);
    }

    @Override
    public String getValue() {
        return (String) value;
    }
}
