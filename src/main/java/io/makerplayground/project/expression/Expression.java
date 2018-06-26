package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Expression {

    public enum Type {
        SIMPLE_STRING, PROJECT_VALUE, NUMBER_WITH_UNIT, NUMBER_IN_RANGE, CUSTOM_NUMBER
    }

    private final Type type;
    private final ObservableList<Term> terms = FXCollections.observableArrayList();
    private final BooleanProperty enable = new SimpleBooleanProperty(false);

    protected Expression(Type type) {
        this.type = type;
    }

    protected Expression(Expression e) {
        terms.addAll(e.terms);  // Term is immutable
        enable.set(e.isEnable());
        type = e.type;
    }

    public static Expression deepCopy(Expression e) {
        if (e instanceof NumberInRangeExpression) {
            return new NumberInRangeExpression((NumberInRangeExpression) e);
        } else if (e instanceof CustomNumberExpression) {
            return new CustomNumberExpression((CustomNumberExpression) e);
        } else if (e instanceof NumberWithUnitExpression) {
            return new NumberWithUnitExpression((NumberWithUnitExpression) e);
        } else if (e instanceof ProjectValueExpression) {
            return new ProjectValueExpression((ProjectValueExpression) e);
        } else if (e instanceof SimpleStringExpression) {
            return new SimpleStringExpression((SimpleStringExpression) e);
        } else {
            throw new IllegalStateException("Not support type of expression");
        }
    }

    public static Expression fromDefaultParameter(Parameter param) {
        switch (param.getDataType()) {
            case DOUBLE:
                return new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue());
            case INTEGER:
                return new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue());
            case STRING:
                return new SimpleStringExpression((String) param.getDefaultValue());
            case ENUM:
                return new SimpleStringExpression((String) param.getDefaultValue());
            case VALUE:
                return new ProjectValueExpression();
        }

        throw new IllegalStateException("implement the Expression Selection here");
    }

    public ObservableList<Term> getTerms() {
        return terms;
    }

    public void clearTerm() {
        terms.clear();
    }

    @JsonIgnore
    public Set<ProjectValue> getValueUsed() {
        return terms.stream().filter(term -> term.getType() == Term.Type.VALUE)
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

    @JsonIgnore
    public boolean isValid() {
        return !terms.isEmpty();
    }

    public String translateToCCode() {
        List<String> termStr = getTerms().stream().map(Term::toCCode).collect(Collectors.toList());
        return String.join("", termStr);
    }
}
