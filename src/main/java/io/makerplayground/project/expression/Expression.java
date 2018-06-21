package io.makerplayground.project.expression;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.chip.ChipType;
import io.makerplayground.project.chip.Term;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;
import java.util.stream.Collectors;

public class Expression {
    private final ObservableList<Term> terms = FXCollections.observableArrayList();
    private final BooleanProperty enable = new SimpleBooleanProperty(false);

    public Expression() {
    }

    protected Expression(Expression e) {
        terms.addAll(e.terms);  // Term is immutable
        enable.set(e.isEnable());
    }

    public static Expression newInstance(Expression e) {
        if (e instanceof NumberInRangeExpression) {
            return new NumberInRangeExpression((NumberInRangeExpression) e);
        } else {
            return new Expression(e);
        }
    }

    public static Expression fromParameter(Parameter param) {
        switch (param.getDataType()) {

            case DOUBLE:
                return new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue());
//                break;
            case STRING:
                return new SimpleStringExpression((String) param.getDefaultValue());
//                break;
            case ENUM:
                return new SimpleStringExpression((String) param.getDefaultValue());
//                break;
            case INTEGER:
                return new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue());
//                break;
            case VALUE:
                return new ProjectValueExpression(null);
//                break;
        }

        throw new IllegalStateException("implement the Expression Selection here");
    }

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
