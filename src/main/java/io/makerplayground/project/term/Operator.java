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

package io.makerplayground.project.term;

public enum Operator {
    PLUS(OperatorType.BINARY, "+"),
    MINUS(OperatorType.BINARY, "-"),
    MULTIPLY(OperatorType.BINARY, "*", "x"),
    DIVIDE(OperatorType.BINARY, "/"),
    GREATER_THAN(OperatorType.BINARY, ">"),
    LESS_THAN(OperatorType.BINARY, "<"),
    GREATER_THAN_OR_EQUAL(OperatorType.BINARY, ">=", "\u2265"),
    LESS_THAN_OR_EQUAL(OperatorType.BINARY, "<=", "\u2264"),
    AND(OperatorType.BINARY, "&&", "and"),
    OR(OperatorType.BINARY, "||", "or"),
    NOT(OperatorType.LEFT_UNARY, "!", "not"),
    OPEN_PARENTHESIS(OperatorType.LEFT_UNARY, "("),
    CLOSE_PARENTHESIS(OperatorType.RIGHT_UNARY, ")"),
    EQUAL(OperatorType.BINARY, "="),
    NOT_EQUAL(OperatorType.BINARY, "!=", "\u2260");

    private final OperatorType type;
    private final String codeString;
    private final String displayString;

    Operator(OperatorType type, String codeString) {
        this(type, codeString, codeString);
    }

    Operator(OperatorType type, String codeString, String displayString) {
        this.type = type;
        this.codeString = codeString;
        this.displayString = displayString;
    }

    public OperatorType getType() {
        return type;
    }

    public String getDisplayString() {
        return displayString;
    }

    public String getCodeString() {
        return codeString;
    }

    @Override
    public String toString() {
        return displayString;
    }
}
