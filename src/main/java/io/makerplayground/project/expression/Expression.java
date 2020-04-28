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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Record;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import javafx.beans.binding.StringExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Expression {

    public enum Type {
        SIMPLE_INTEGER, SIMPLE_STRING, COMPLEX_STRING, PROJECT_VALUE, NUMBER_WITH_UNIT, NUMBER_IN_RANGE, CONDITIONAL, CUSTOM_NUMBER, DATETIME, VALUE_LINKING, IMAGE, VARIABLE, RECORD, STRING_INT, DOT_MATRIX;
    }

    public enum RefreshInterval {
        ONCE, USER_DEFINED, REALTIME;

        @Override
        public String toString() {
            switch (this) {
                case ONCE: return "once";
                case USER_DEFINED: return "once and every";
                case REALTIME: return "realtime";
                default: throw new IllegalStateException();
            }
        }
    }

    private final Type type;
    protected final List<Term> terms = new ArrayList<>();
    private RefreshInterval refreshInterval;
    private NumberWithUnit userDefinedInterval;

    public Expression(Type type) {
        this(type, RefreshInterval.ONCE, NumberWithUnit.ZERO_SECOND);
    }

    @JsonCreator
    private Expression(Type type, RefreshInterval refreshInterval, NumberWithUnit userDefinedInterval) {
        this(type, refreshInterval, userDefinedInterval, new ArrayList<>());
    }

    private Expression(Type type, RefreshInterval refreshInterval, NumberWithUnit userDefinedInterval, List<Term> terms) {
        this.type = type;
        this.refreshInterval = refreshInterval;
        this.userDefinedInterval = userDefinedInterval;
        this.terms.addAll(terms);
    }

    protected Expression(Expression e) {
        this(e.type, e.refreshInterval, e.userDefinedInterval, e.terms);
    }

    public abstract Expression deepCopy();

    public static Expression fromDefaultParameter(Parameter param) {
        switch (param.getDataType()) {
            case DOUBLE:
            case INTEGER:
                return new CustomNumberExpression((NumberWithUnit) param.getDefaultValue());
            case INTEGER_ENUM:
                return new SimpleIntegerExpression((Integer) param.getDefaultValue());
            case STRING:
                return new ComplexStringExpression((String) param.getDefaultValue());
            case DATETIME:
                return new SimpleRTCExpression(RealTimeClock.getDefault());
            case IMAGE:
                return new ImageExpression();
            case RECORD:
                return new RecordExpression(new Record());
            case STRING_INT_ENUM:
                return new StringIntegerExpression((StringIntegerCategoricalConstraint) param.getConstraint(), (String) param.getDefaultValue());
            case DOT_MATRIX_DATA:
                return new DotMatrixExpression(new DotMatrix());
            case VARIABLE_NAME:
                return VariableExpression.NO_VARIABLE_SELECTED;
            default:
                throw new IllegalStateException("Cannot create expression from default parameter: " + param);
        }
    }

    public Type getType() {
        return type;
    }

    public List<Term> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    public RefreshInterval getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(RefreshInterval interval) {
        refreshInterval = interval;
        // reset user defined interval when refresh interval is not USER_DEFINED
        if (refreshInterval != RefreshInterval.USER_DEFINED) {
            userDefinedInterval = NumberWithUnit.ZERO_SECOND;
        }
    }

    public NumberWithUnit getUserDefinedInterval() {
        return userDefinedInterval;
    }

    public void setUserDefinedInterval(NumberWithUnit userDefinedInterval) {
        this.userDefinedInterval = userDefinedInterval;
    }

    @JsonIgnore
    public Set<ProjectValue> getValueUsed() {
        return terms.stream().filter(term -> term.getType() == Term.Type.VALUE)
                .map(term -> (ProjectValue) term.getValue())
                .collect(Collectors.toUnmodifiableSet());
    }

    @JsonIgnore
    public boolean isValid() {
        return !terms.isEmpty() && terms.stream().allMatch(Term::isValid);
    }
}
