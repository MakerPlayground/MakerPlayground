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

package io.makerplayground.device.shared.constraint;

import io.makerplayground.device.shared.Unit;

import java.util.*;

/**
 * Represent a constraint for a numeric value
 */
public class CategoricalConstraint implements Constraint {
    private final Collection<String> categoricalValue;

    /**
     * Construct a constraint to match a specify string. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param s the string to be matched by this constraint
     */
    CategoricalConstraint(String s) {
        this.categoricalValue = Collections.singletonList(s);
    }

    /**
     * Construct a constraint to match a list of strings given. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param value the list of strings value to be matched
     */
    CategoricalConstraint(Collection<String> value) {
        this.categoricalValue = value;
    }

    public Collection<String> getCategories() {
        return categoricalValue;
    }

    @Override
    public boolean test(double d, Unit unit) {
        return false;
    }

    @Override
    public boolean test(String s) {
        return categoricalValue.contains(s);
    }

    @Override
    public boolean isCompatible(Constraint constraint) {
        if (!(constraint instanceof CategoricalConstraint))
            return false;

        return categoricalValue.containsAll(((CategoricalConstraint) constraint).categoricalValue);
    }

    @Override
    public Constraint union(Constraint constraint) {
        if (!(constraint instanceof CategoricalConstraint))
            throw new ClassCastException();

        CategoricalConstraint categoricalConstraint = (CategoricalConstraint) constraint;
        Set<String> tmp = new HashSet<>(categoricalValue);
        tmp.addAll(categoricalConstraint.categoricalValue);
        return new CategoricalConstraint(tmp);
    }

    @Override
    public Constraint intersect(Constraint constraint) {
        if (constraint == Constraint.NONE) {
            return Constraint.NONE;
        }

        if (!(constraint instanceof CategoricalConstraint))
            throw new ClassCastException();

        CategoricalConstraint categoricalConstraint = (CategoricalConstraint) constraint;
        Set<String> tmp = new HashSet<>(categoricalValue);
        tmp.retainAll(categoricalConstraint.categoricalValue);
        return new CategoricalConstraint(tmp);
    }

    @Override
    public String toString() {
        return "CategoricalConstraint{" +
                "categoricalValue=" + categoricalValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoricalConstraint that = (CategoricalConstraint) o;
        return this.categoricalValue.containsAll(that.categoricalValue) && that.categoricalValue.containsAll(this.categoricalValue);
    }

    @Override
    public int hashCode() {

        return Objects.hash(categoricalValue);
    }
}
