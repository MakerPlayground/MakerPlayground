package io.makerplayground.generator.source;

import io.makerplayground.project.term.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class NumberExpressionParser {
    // support +, - , *, /, %
    private static boolean isHigherPrecedence(Operator op1, Operator op2) {
        return (op1 == Operator.MULTIPLY || op1 == Operator.DIVIDE || op1 == Operator.DIVIDE_INT || op1 == Operator.MOD)
            && (op2 == Operator.PLUS || op2 == Operator.MINUS);
    }

    public static NumberExpressionTreeNode parseExpression(List<Term> terms) {
        if (!terms.stream().allMatch(t -> t instanceof OperatorTerm || t instanceof NumberWithUnitTerm || t instanceof ValueTerm)) {
            throw new IllegalStateException("Found unsupported term's type");
        }

        // convert from infix to postfix expression
        List<Term> postfixExpression = new ArrayList<>();
        Deque<OperatorTerm> operatorStack = new ArrayDeque<>();
        for (Term t : terms) {
            if (t instanceof NumberWithUnitTerm || t instanceof ValueTerm) {
                postfixExpression.add(t);
            } else if (t instanceof OperatorTerm) {
                Operator current = ((OperatorTerm) t).getValue();
                if (current == Operator.OPEN_PARENTHESIS) {
                    operatorStack.addFirst((OperatorTerm) t);
                } else if (current == Operator.CLOSE_PARENTHESIS) {
                    while (operatorStack.peekFirst().getValue() != Operator.OPEN_PARENTHESIS) {
                        postfixExpression.add(operatorStack.removeFirst());
                    }
                    operatorStack.removeFirst();
                } else {
                    while (!operatorStack.isEmpty() && operatorStack.peekFirst().getValue() != Operator.OPEN_PARENTHESIS
                            && isHigherPrecedence(operatorStack.peekFirst().getValue(), current)) {
                        postfixExpression.add(operatorStack.removeFirst());
                    }
                    operatorStack.addFirst((OperatorTerm) t);
                }
            } else {
                throw new IllegalStateException("Found unsupported term's type");
            }
        }
        while (!operatorStack.isEmpty()) {
            postfixExpression.add(operatorStack.removeFirst());
        }

        // convert from postfix expression into an abstract syntax tree
        Deque<NumberExpressionTreeNode> operandStack = new ArrayDeque<>();
        for (Term t : postfixExpression) {
            if (t instanceof NumberWithUnitTerm || t instanceof ValueTerm) {
                operandStack.addFirst(new NumberExpressionTreeNode(t));
            } else {
                NumberExpressionTreeNode op2 = operandStack.removeFirst();
                NumberExpressionTreeNode op1 = operandStack.removeFirst();
                operandStack.addFirst(new NumberExpressionTreeNode(op1, t, op2));
            }
        }
        if (operandStack.size() != 1) {
            throw new IllegalStateException("Can't parse expression: " + postfixExpression);
        }
        return operandStack.removeFirst();
    }
}
