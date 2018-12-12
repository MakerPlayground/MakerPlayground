/*
 * Copyright (c) 2018. The Maker Playground Authors.
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
import java.util.List;

public class CustomNumberExpression extends Expression {

    public CustomNumberExpression() {
        super(Type.CUSTOM_NUMBER);
    }

    public CustomNumberExpression(List<Term> terms) {
        super(Type.CUSTOM_NUMBER);
        this.terms.addAll(terms);
    }

    CustomNumberExpression(CustomNumberExpression e) {
        super(e);
    }

    public CustomNumberExpression addTerm(int index, Term t) {
        List<Term> newTerm = new ArrayList<>(terms);
        newTerm.add(index, t);
        return new CustomNumberExpression(newTerm);
    }

    public CustomNumberExpression removeTerm(int index) {
        List<Term> newTerm = new ArrayList<>(terms);
        newTerm.remove(index);
        return new CustomNumberExpression(newTerm);
    }

    @Override
    public boolean isValid() {
        List<Term> terms = getTerms();
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

    @Override
    public CustomNumberExpression deepCopy() {
        return new CustomNumberExpression(this);
    }

}
