package io.makerplayground.project.expression;

import io.makerplayground.project.ProjectValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;
import java.util.stream.Collectors;

public class Expression {
    private final ObservableList<Term> terms = FXCollections.observableArrayList();
    private final BooleanProperty enable = new SimpleBooleanProperty(false);

    public ObservableList<Term> getTerms() {
        return terms;
    }

    public void clearTerm() {
        terms.clear();
    }

    public Set<ProjectValue> getValueUsed() {
        return terms.stream().filter(term -> term.getType() == ChipType.VALUE)
                .map(term -> (ProjectValue) term.getValue())
                .collect(Collectors.toSet());
    }

    public boolean isEnable() {
        return enable.get();
    }

    public BooleanProperty enableProperty() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable.set(enable);
    }

    public boolean isValid() {
        return !terms.isEmpty();
    }
}
