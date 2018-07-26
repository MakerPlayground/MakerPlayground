package io.makerplayground.project.term;

public class OperatorTerm extends Term {

    public OperatorTerm(OP value) {
        super(Term.Type.OPERATOR, value);
    }

    @Override
    public OP getValue() {
        return (OP) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    public enum OP {
        PLUS, MINUS, MULTIPLY, DIVIDE,
        GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
        AND, OR, NOT, OPEN_PARENTHESIS, CLOSE_PARENTHESIS,TEST;

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
}


