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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonDeserialize(using = ParameterDeserializer.class)
public class Parameter {
    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final DataType dataType;
    private final ControlType controlType;

    public Parameter(String name, Object defaultValue, Constraint constraint, DataType dataType, ControlType controlType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.dataType = dataType;
        this.controlType = controlType;
    }

    public double getMinimumValue() {
        return ((NumericConstraint) constraint).getMin();
    }

    public double getMaximumValue() {
        return ((NumericConstraint) constraint).getMax();
    }

    public List<Unit> getUnit() {
        return Collections.singletonList(((NumericConstraint) constraint).getUnit());  // TODO: add another convertible unit
    }
}
