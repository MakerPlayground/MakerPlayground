package io.makerplayground.project.expression;

import io.makerplayground.project.ProjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;
import java.util.stream.Collectors;

public class Expression {
    private final ObservableList<Term> terms = FXCollections.observableArrayList();

    public ObservableList<Term> getTerms() {
        return terms;
    }

    public Set<ProjectValue> getValueUsed() {
        return terms.stream().filter(term -> term.getType() == ChipType.VALUE)
                .map(term -> (ProjectValue) term.getValue())
                .collect(Collectors.toSet());
    }
}
