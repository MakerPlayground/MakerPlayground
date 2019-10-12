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

package io.makerplayground.device.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class NumberWithUnit {
    private double d;
    private Unit u;

    public static final NumberWithUnit ZERO = new NumberWithUnit(0, Unit.NOT_SPECIFIED);
    public static final NumberWithUnit ZERO_SECOND = new NumberWithUnit(0, Unit.SECOND);

    public NumberWithUnit(double d, Unit u) {
        this.d = d;
        this.u = u;
    }

    @JsonCreator
    public static NumberWithUnit of(@JsonProperty("value") double d, @JsonProperty("unit") Unit u) {
        if (d == 0 && u == Unit.NOT_SPECIFIED) {
            return ZERO;
        } else if (d == 0 && u == Unit.SECOND) {
            return ZERO_SECOND;
        } else {
            return new NumberWithUnit(d, u);
        }
    }

    public double getValue() {
        return d;
    }

    public Unit getUnit() {
        return u;
    }

    @Override
    public String toString() {
        return String.valueOf(d) + " " + u.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberWithUnit that = (NumberWithUnit) o;
        return Double.compare(that.d, d) == 0 &&
                u == that.u;
    }

    @Override
    public int hashCode() {
        return Objects.hash(d, u);
    }
}
