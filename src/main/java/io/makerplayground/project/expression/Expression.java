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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Expression {

    public enum Type {
        SIMPLE_STRING, PROJECT_VALUE, NUMBER_WITH_UNIT, NUMBER_IN_RANGE, CUSTOM_NUMBER, VALUE_LINKING
    }

    private final Type type;
    protected final List<Term> terms = new ArrayList<>();
    private final BooleanProperty enable = new SimpleBooleanProperty(false);

    public Expression(Type type) {
        this.type = type;
    }

    protected Expression(Expression e) {
        this(e.type);
        terms.addAll(e.terms);  // Term is immutable
        enable.set(e.isEnable());
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
        } else if (e instanceof ValueLinkingExpression) {
            return new ValueLinkingExpression((ValueLinkingExpression) e);
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

    public List<Term> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    @JsonIgnore
    public Set<ProjectValue> getValueUsed() {
        return terms.stream().filter(term -> term.getType() == Term.Type.VALUE)
                .map(term -> (ProjectValue) term.getValue())
                .collect(Collectors.toUnmodifiableSet());
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
        return !terms.isEmpty() && terms.stream().allMatch(Term::isValid);
    }

    public String translateToCCode() {
        List<String> termStr = getTerms().stream().map(Term::toCCode).collect(Collectors.toList());
        return String.join(" ", termStr);
    }
}
