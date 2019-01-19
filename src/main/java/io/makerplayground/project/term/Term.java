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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public abstract class Term {

    public enum Type {
        NUMBER, STRING, VALUE, OPERATOR, DATETIME, RECORD
    }
    private final Type type;
    protected final Object value;

    public Term(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public abstract Object getValue();

    @JsonIgnore
    public abstract boolean isValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return type == term.type &&
                Objects.equals(value, term.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
