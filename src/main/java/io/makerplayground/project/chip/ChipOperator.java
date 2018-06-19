package io.makerplayground.project.chip;

public enum ChipOperator {
    PLUS, MINUS, MULTIPLY, DIVIDE,
    GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
    AND, OR, NOT, OPEN_PARENTHESIS, CLOSE_PARENTHESIS;

    @Override
    public String toString() {
        switch (this) {
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MULTIPLY:
                return "x";
            case DIVIDE:
                return "/";
            case GREATER_THAN:
                return ">";
            case LESS_THAN:
                return "<";
            case GREATER_THAN_OR_EQUAL:
                return ">=";
            case LESS_THAN_OR_EQUAL:
                return "<=";
            case AND:
                return "and";
            case OR:
                return "or";
            case NOT:
                return "!";
            case OPEN_PARENTHESIS:
                return "(";
            case CLOSE_PARENTHESIS:
                return ")";
            default:
                throw new IllegalStateException("Unknown enum constant");
        }
    }
}
