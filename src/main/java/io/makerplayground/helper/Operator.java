/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.helper;

/**
 * An enum represent an operator ex. >, <, <=, >=, =, etc.
 */
public enum Operator {
    GREATER_THAN_LITERAL ("> numeral", ">"),
    LESS_THAN_LITERAL("< numeral", "<"),
    GREATER_THAN_OR_EQUAL_LITERAL(">= numeral", ">="),
    LESS_THAN_OR_EQUAL_LITERAL("<= numeral", "<="),
    EQUAL_LITERAL("= numeral", "=="),
    GREATER_THAN_VARIABLE("> variable", ">"),
    LESS_THAN_VARIABLE("< variable", "<"),
    GREATER_THAN_OR_EQUAL_VARIABLE(">= variable", ">="),
    LESS_THAN_OR_EQUAL_VARIABLE("<= variable", "<="),
    EQUAL_VARIABLE("= variable", "=="),
    BETWEEN_LITERAL("Between number", ""),
    BETWEEN_VARIABLE("Between variable", "");

    private final String displayValue;
    private final String codeValue;

    Operator(String displayValue, String codeValue) {
        this.displayValue = displayValue;
        this.codeValue = codeValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

    @Override
    public String toString() {
        return this.displayValue;
    }

    public boolean isLiteral() {
        return (this == GREATER_THAN_LITERAL) || (this == LESS_THAN_LITERAL) || (this == GREATER_THAN_OR_EQUAL_LITERAL)
                || (this == LESS_THAN_OR_EQUAL_LITERAL) || (this == EQUAL_LITERAL);
    }

    public boolean isVariable() {
        return (this == GREATER_THAN_VARIABLE)|| (this ==  LESS_THAN_VARIABLE) || (this ==  GREATER_THAN_OR_EQUAL_VARIABLE)
                || (this == LESS_THAN_OR_EQUAL_VARIABLE) || (this ==  EQUAL_VARIABLE );
    }

    public boolean isBetween() {
        return (this == BETWEEN_LITERAL) || (this == BETWEEN_VARIABLE);
    }
}
