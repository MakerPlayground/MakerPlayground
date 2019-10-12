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

import io.makerplayground.device.shared.Value;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConditionalExpression extends Expression {

    private final ProjectDevice projectDevice;
    private final Value value;
    private final List<Entry> entries;

    public ConditionalExpression(ProjectDevice projectDevice, Value value) {
        super(Type.CONDITIONAL);
        this.projectDevice = projectDevice;
        this.value = value;
        this.entries = new ArrayList<>();
    }

    public ConditionalExpression(ProjectDevice projectDevice, Value value, List<Term> terms) {
        super(Type.CONDITIONAL);
        this.projectDevice = projectDevice;
        this.value = value;
        this.terms.addAll(terms);
        this.entries = new ArrayList<>();

        // initialize the entries list
        List<Integer> index = IntStream.range(0, terms.size())
                .filter(i -> (terms.get(i) instanceof OperatorTerm))
                .filter(i -> Operator.getComparisonOperator().contains(((OperatorTerm) (terms.get(i))).getValue()))
                .boxed().collect(Collectors.toList());
        for (int i=0; i<index.size()-1; i++) {
            entries.add(termToEntry(terms.subList(index.get(i)-2, index.get(i+1)-2)));
        }
        entries.add(termToEntry(terms.subList(index.get(index.size()-1)-2, terms.size())));
    }

    ConditionalExpression(ConditionalExpression e) {
        super(e);
        this.projectDevice = e.projectDevice;
        this.value = e.value;
        this.entries = e.entries.stream().map(Entry::new).collect(Collectors.toList());
    }

    public ProjectDevice getProjectDevice() {
        return projectDevice;
    }

    public Value getValue() {
        return value;
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
        return !entries.isEmpty() && entries.stream().allMatch(entry -> entry.getExpression().isValid());
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
        termList.add(new ValueTerm(new ProjectValue(projectDevice, value)));
        termList.add(new OperatorTerm(entry.getOperator()));
        termList.addAll(entry.getExpression().getTerms());
        termList.add(new OperatorTerm(Operator.CLOSE_PARENTHESIS));
        return termList;
    }

    private Entry termToEntry(List<Term> termList) {
        Term term = termList.get(0);
        if (!(term instanceof OperatorTerm && term.getValue() == Operator.OPEN_PARENTHESIS)) {
            throw new IllegalStateException();
        }
        ProjectValue projectValue = ((ValueTerm) termList.get(1)).getValue();
        if (projectValue.getDevice() != projectDevice || projectValue.getValue() != value) {
            throw new IllegalStateException();
        }
        Operator operator = ((OperatorTerm) termList.get(2)).getValue();
        CustomNumberExpression expression = new CustomNumberExpression(termList.subList(3, termList.size() - 1));
        Term term2 = termList.get(termList.size()-1);
        if (!(term2 instanceof OperatorTerm && term2.getValue() == Operator.CLOSE_PARENTHESIS)) {
            throw new IllegalStateException();
        }
        return new Entry(operator, expression);
    }

    public static class Entry {
        private Operator operator;
        private CustomNumberExpression expression;

        public Entry(Operator operator, CustomNumberExpression expression) {
            this.operator = operator;
            this.expression = expression;
        }

        public Entry(Entry e) {
            this.operator = e.operator;
            this.expression = e.expression;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public CustomNumberExpression getExpression() {
            return expression;
        }

        public void setExpression(CustomNumberExpression expression) {
            this.expression = expression;
        }
    }

    @Override
    public ConditionalExpression deepCopy() {
        return new ConditionalExpression(this);
    }
}
