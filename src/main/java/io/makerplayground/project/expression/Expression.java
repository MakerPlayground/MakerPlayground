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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Expression {

    public enum Type {
        SIMPLE_STRING, PROJECT_VALUE, NUMBER_WITH_UNIT, NUMBER_IN_RANGE, CUSTOM_NUMBER, VALUE_LINKING
    }

    public enum RefreshInterval {
        ONCE, USER_DEFINED, REALTIME;

        @Override
        public String toString() {
            switch (this) {
                case ONCE: return "Set once";
                case REALTIME: return "Update realtime";
                case USER_DEFINED: return "Update every";
                default: throw new IllegalStateException();
            }
        }
    }

    private final Type type;
    protected final List<Term> terms = new ArrayList<>();
    private RefreshInterval refreshInterval;
    private NumberWithUnit userDefinedInterval;

    public Expression(Type type) {
        this.type = type;
        this.refreshInterval = RefreshInterval.ONCE;
        this.userDefinedInterval = NumberWithUnit.ZERO_SECOND;
    }

    @JsonCreator
    private Expression(Type type, RefreshInterval refreshInterval, NumberWithUnit userDefinedInterval) {
        this.type = type;
        this.refreshInterval = refreshInterval;
        this.userDefinedInterval = userDefinedInterval;
    }

    protected Expression(Expression e) {
        this(e.type);
        terms.addAll(e.terms);  // Term is immutable
        refreshInterval = e.refreshInterval;
        userDefinedInterval = e.userDefinedInterval;
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

    public String translateToCCode() {
        List<String> termStr = getTerms().stream().map(Term::toCCode).collect(Collectors.toList());
        return String.join(" ", termStr);
    }
}
