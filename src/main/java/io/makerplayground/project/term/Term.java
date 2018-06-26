package io.makerplayground.project.term;

public abstract class Term {

    public enum Type {
        NUMBER, STRING, VALUE, OPERATOR,
    }
    private final Type type;
    protected final Object value;

    public Term(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public abstract Object getValue();
}
