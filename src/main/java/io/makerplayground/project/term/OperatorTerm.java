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
        switch (this.getValue()) {
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MULTIPLY:
                return "*";
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
                return "&&";
            case OR:
                return "||";
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

    public enum OperatorType {
        BINARY, LEFT_UNARY, RIGHT_UNARY
    }

    public enum Operator {
        PLUS("+", OperatorType.BINARY),
        MINUS("-", OperatorType.BINARY),
        MULTIPLY("x", OperatorType.BINARY),
        DIVIDE("\u00F7", OperatorType.BINARY),
        GREATER_THAN(">", OperatorType.BINARY),
        LESS_THAN("<", OperatorType.BINARY),
        GREATER_THAN_OR_EQUAL(">=", OperatorType.BINARY),
        LESS_THAN_OR_EQUAL("<=", OperatorType.BINARY),
        AND("and", OperatorType.BINARY),
        OR("or", OperatorType.BINARY),
        NOT("not", OperatorType.LEFT_UNARY),
        OPEN_PARENTHESIS("(", OperatorType.LEFT_UNARY),
        CLOSE_PARENTHESIS(")", OperatorType.RIGHT_UNARY);

        private final String name;
        private final OperatorType type;

        Operator(String name, OperatorType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }

        public OperatorType getType() {
            return type;
        }
    }
}


