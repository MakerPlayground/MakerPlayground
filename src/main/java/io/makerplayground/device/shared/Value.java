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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.shared.constraint.Constraint;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@JsonDeserialize(using = ValueDeserializer.class)
public class Value {
    private StringProperty name;

    private final DataType type;
    private final Constraint constraint;

    /**
     * Construct a new value. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param name name of the value ex. temp, accel_x, etc.
     * @param type an enumerated value ({@link DataType}) indicating type of a value
     * @param constraint an enumerated value ({@link Constraint}) indicating type of a constraint
     */
    @JsonCreator
    public Value(String name, DataType type, Constraint constraint) {
        this.name = new SimpleStringProperty(name);
        this.type = type;
        this.constraint = constraint;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public DataType getType() {
        return type;
    }

    public Constraint getConstraint() {
        return constraint;
    }
}
