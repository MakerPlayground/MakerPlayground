package io.makerplayground.project.term;

public class StringTerm extends Term {
    public StringTerm(String value) {
        super(Type.STRING, value);
    }

    @Override
    public String getValue() {
        return (String) value;
    }
}
