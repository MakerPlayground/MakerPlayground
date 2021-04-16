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

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Operator;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BooleanExpression extends Expression {

    private final List<Entry> entries;

    public BooleanExpression() {
        super(Type.BOOLEAN);
        this.entries = new ArrayList<>();
        this.entries.add(new Entry(new CustomNumberExpression(), Operator.LESS_THAN, new CustomNumberExpression()));
    }

    public BooleanExpression(List<Term> terms) {
        super(Type.BOOLEAN);
        this.terms.addAll(terms);
        this.entries = new ArrayList<>();

        // initialize the entries list
        int currentStart = 0;
        for (int i=0; i<terms.size(); i++) {
            if ((terms.get(i) instanceof OperatorTerm) && (terms.get(i).getValue() == Operator.AND)) {
                entries.add(termToEntry(terms.subList(currentStart, i)));
                currentStart = i+1;
            }
        }
        entries.add(termToEntry(terms.subList(currentStart, terms.size())));
    }

    BooleanExpression(BooleanExpression e) {
        super(e);
        this.entries = e.entries.stream().map(Entry::new).collect(Collectors.toList());
    }

    public List<Entry> getEntries() {
        return entries;
    }

    // this method need to be overridden as term need to be regenerated from the entries list as detected change in the
    // entries list to update terms automatically is difficult
    @Override
    public List<Term> getTerms() {
        List<Term> termList = new ArrayList<>();
        if (!entries.isEmpty()) {
            termList.addAll(entryToTerm(entries.get(0)));
            for (int i = 1; i < entries.size(); i++) {
                termList.add(new OperatorTerm(Operator.AND));
                termList.addAll(entryToTerm(entries.get(i)));
            }
        }
        return termList;
    }

    @Override
    public boolean isValid() {
        return !entries.isEmpty() && entries.stream()
                .allMatch(entry -> entry.getFirstOperand().isValid() && entry.getSecondOperand().isValid());
    }

    // this method need to be overridden as term need to be regenerated from the entries list as detected change in the
    // entries list to update terms automatically is difficult
    @Override
    public Set<ProjectValue> getValueUsed() {
        return getTerms().stream().filter(term -> term.getType() == Term.Type.VALUE)
                .map(term -> (ProjectValue) term.getValue())
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<Term> entryToTerm(Entry entry) {
        List<Term> termList = new ArrayList<>();
        termList.add(new OperatorTerm(Operator.OPEN_PARENTHESIS));
        termList.addAll(entry.getFirstOperand().getTerms());
        termList.add(new OperatorTerm(entry.getOperator()));
        termList.addAll(entry.getSecondOperand().getTerms());
        termList.add(new OperatorTerm(Operator.CLOSE_PARENTHESIS));
        return termList;
    }

    private Entry termToEntry(List<Term> termList) {
        Term firstTerm = termList.get(0);
        if (!(firstTerm instanceof OperatorTerm && firstTerm.getValue() == Operator.OPEN_PARENTHESIS)) {
            throw new IllegalStateException("Error: can't convert terms to BooleanExpression's entry");
        }

        Term lastTerm = termList.get(termList.size() - 1);
        if (!(lastTerm instanceof OperatorTerm && lastTerm.getValue() == Operator.CLOSE_PARENTHESIS)) {
            throw new IllegalStateException("Error: can't convert terms to BooleanExpression's entry");
        }

        List<Term> operatorTerms = termList.stream().filter(term -> term instanceof OperatorTerm)
                .filter(term -> Operator.isComparisonOperator(((OperatorTerm) term).getValue()))
                .collect(Collectors.toList());
        if (operatorTerms.size() != 1) {
            throw new IllegalStateException("Error: can't convert terms to BooleanExpression's entry");
        }

        OperatorTerm operatorTerm = (OperatorTerm) operatorTerms.get(0);
        int operatorPos = termList.indexOf(operatorTerm);
        CustomNumberExpression firstOperand = new CustomNumberExpression(termList.subList(1, operatorPos));
        CustomNumberExpression secondOperand = new CustomNumberExpression(termList.subList(operatorPos+1, termList.size()-1));
        return new Entry(firstOperand, operatorTerm.getValue(), secondOperand);
    }

    @Data
    @AllArgsConstructor
    public static class Entry {
        private CustomNumberExpression firstOperand;
        private Operator operator;
        private CustomNumberExpression secondOperand;

        public Entry(Entry e) {
            firstOperand = e.firstOperand.deepCopy();
            operator = e.operator;
            secondOperand = e.secondOperand.deepCopy();
        }
    }

    @Override
    public BooleanExpression deepCopy() {
        return new BooleanExpression(this);
    }
}
