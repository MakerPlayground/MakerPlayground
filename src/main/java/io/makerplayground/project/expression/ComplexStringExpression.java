/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project.expression;

import io.makerplayground.project.term.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplexStringExpression extends Expression {

    public static final ComplexStringExpression INVALID = new ComplexStringExpression(Collections.emptyList());

    public ComplexStringExpression() {
        super(Type.COMPLEX_STRING);
    }

    public ComplexStringExpression(String s) {
        super(Type.COMPLEX_STRING);
        if (!s.isEmpty()) {
            this.terms.add(new StringTerm(s));
        }
    }

    public ComplexStringExpression(List<Term> terms) {
        super(Type.COMPLEX_STRING);
        this.terms.addAll(terms);
    }

    ComplexStringExpression(ComplexStringExpression e) {
        super(e);
    }

    @Override
    public boolean isValid() {
        int i=0, startIndex=0, endIndex=0;
        while (i < terms.size()) {
            // advance to the first non string term if any
            while ((i < terms.size()) && (terms.get(i) instanceof StringTerm)) {
                i++;
            }
            if (i == terms.size()) {
                break;
            }
            startIndex = i;

            // advance to the next string term after the group of non string term
            while ((i < terms.size()) && !(terms.get(i) instanceof StringTerm)) {
                i++;
            }
            endIndex = i;

            // check non string term between startIndex (inclusive) and endIndex (exclusive)
            if (!isNumericTermsValid(terms.subList(startIndex, endIndex))) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumericTermsValid(List<Term> terms) {
        if (terms.isEmpty()) {
            return false;
        }

        /* check parenthesis */
        int countParen = 0;
        for (Term term : terms) {
            if (term instanceof OperatorTerm) {
                if (Operator.OPEN_PARENTHESIS.equals(term.getValue())) { countParen++; }
                else if(Operator.CLOSE_PARENTHESIS.equals(term.getValue())) {
                    countParen--;
                    if (countParen < 0) { return false; }
                }
            }
        }
        if (countParen != 0) { return false; }
        /* check each term */
        for (Term t: terms) {
            if (!t.isValid()) {
                return false;
            }
        }

        /* check first item in the sequence */
        if (isOperationNotParenTerm(terms.get(0))) {
            return false;
        }

        /* check valid sequence */
        for (int i=0; i<terms.size()-1; i++) {
            Term term = terms.get(i);
            Term nextTerm = terms.get(i+1);
            if (isNumberOrValueTerm(term)) {
                if (isNumberOrValueTerm(nextTerm) || Operator.OPEN_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (isOperationNotParenTerm(term)) {
                if (isOperationNotParenTerm(nextTerm) || Operator.CLOSE_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (Operator.OPEN_PARENTHESIS.equals(term.getValue())) {
                if (isOperationNotParenTerm(nextTerm) || Operator.CLOSE_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (Operator.CLOSE_PARENTHESIS.equals(term.getValue())) {
                if (isNumberOrValueTerm(nextTerm) || Operator.OPEN_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            }
        }
        /* check last */
        if (terms.size() > 0) {
            Term last = terms.get(terms.size()-1);
            if (Operator.OPEN_PARENTHESIS.equals(last.getValue()) || isOperationNotParenTerm(last)) {
                return false;
            }
        }
        return true;
    }

    private boolean isParenTerm(Term t) {
        return t instanceof OperatorTerm &&
                (Operator.OPEN_PARENTHESIS.equals(t.getValue()) || Operator.CLOSE_PARENTHESIS.equals(t.getValue()));
    }

    private boolean isOperationNotParenTerm(Term t) {
        return t instanceof OperatorTerm && !isParenTerm(t);
    }

    private boolean isNumberOrValueTerm(Term t) {
        return t instanceof ValueTerm || t instanceof NumberWithUnitTerm;
    }

    // TODO: should we add json ignore
    public List<Expression> getSubExpressions() {
        List<Expression> result = new ArrayList<>();
        int i=0, startIndex=0;
        while (i < terms.size()) {
            if (terms.get(i) instanceof StringTerm) {
                result.add(new SimpleStringExpression((StringTerm) terms.get(i)));
                i++;
            } else if (terms.get(i) instanceof StringAnimationTerm) {
                result.add(new AnimatedStringExpression((StringAnimationTerm) terms.get(i)));
                i++;
            } else {
                startIndex = i;
                // advance to the next string term after the group of non string term
                while ((i < terms.size()) && !(terms.get(i) instanceof StringTerm) && !(terms.get(i) instanceof StringAnimationTerm)) {
                    i++;
                }
                result.add(new CustomNumberExpression(terms.subList(startIndex, i)));
            }
        }
        return result;
    }

    @Override
    public ComplexStringExpression deepCopy() {
        return new ComplexStringExpression(this);
    }
}
