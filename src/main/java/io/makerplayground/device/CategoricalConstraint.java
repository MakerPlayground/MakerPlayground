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

package io.makerplayground.device;

import io.makerplayground.helper.Unit;

import java.util.Collection;
import java.util.Collections;

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
    public String toString() {
        return "CategoricalConstraint{" +
                "categoricalValue=" + categoricalValue +
                '}';
    }
}
