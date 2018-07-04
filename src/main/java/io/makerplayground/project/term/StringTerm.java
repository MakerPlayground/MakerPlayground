package io.makerplayground.project.term;

public class StringTerm extends Term {
    public StringTerm(String value) {
        super(Type.STRING, value);
    }

    @Override
    public String getValue() {
        return (String) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toCCode(){
        return "\"" + value + "\"";
    }
}
