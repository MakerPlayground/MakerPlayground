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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.Unit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Represent a parameter of an action
 */
@JsonDeserialize(using = ParameterDeserializer.class)
public class Parameter {
    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final DataType dataType;
    private final ControlType controlType;

    /**
     * The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param name name of this parameter ex. brightness
     * @param defaultValue the default value of this parameter ex. 100
     * @param constraint the constraint of this parameter as an instance of {@link Constraint}
     * @param dataType an enumerated value ({@link DataType}) indicating type of this parameter
     * @param controlType an enumerated value ({@link ControlType}) indicating type of a UI control to be used
     */
    Parameter(String name, Object defaultValue, Constraint constraint, DataType dataType, ControlType controlType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.dataType = dataType;
        this.controlType = controlType;
    }

    /**
     * Get the name of this parameter
     * @return name of this parameter ex. brightness, speed, etc.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the default value of this parameter
     * @return the default value of this parameter
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the constraint of this parameter
     * @return the constrint of this parameter
     */
    public Constraint getConstraint() {
        return constraint;
    }

    public double getMinimumValue() {
        return ((NumericConstraint) constraint).getMin();
    }

    public double getMaximumValue() {
        return ((NumericConstraint) constraint).getMax();
    }

    public Collection<Unit> getUnit() {
        return Collections.singletonList(((NumericConstraint) constraint).getUnit());  // TODO: add another convertible unit
    }

    /**
     * Get the datatype of this parameter as an instance of {@link DataType}
     * @return the datatype of this parameter
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Get the suggest control type of this parameter as an instance of {@link ControlType}
     * @return the type of control to be used to adjust this parameter in the GUI
     */
    public ControlType getControlType() {
        return controlType;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", defaultValue=" + defaultValue +
                ", constraint=" + constraint +
                ", dataType=" + dataType +
                ", controlType=" + controlType +
                '}';
    }
}
