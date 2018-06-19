package io.makerplayground.project.expression;

public class SimpleStringExpression extends Expression {
    private final String str;

    public SimpleStringExpression(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
