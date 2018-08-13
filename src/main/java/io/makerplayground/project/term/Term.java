package io.makerplayground.project.term;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public abstract class Term {

    public abstract String toCCode();

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

    @JsonIgnore
    public abstract boolean isValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return type == term.type &&
                Objects.equals(value, term.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
