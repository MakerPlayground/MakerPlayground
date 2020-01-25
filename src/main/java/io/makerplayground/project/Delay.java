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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.shared.DelayUnit;
import javafx.beans.property.*;

@JsonSerialize(using = DelaySerializer.class)
public class Delay extends NodeElement {

    private final DoubleProperty delayValue = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<DelayUnit> delayUnit = new SimpleObjectProperty<>(DelayUnit.MILLISECOND);

    Delay(Project project) {
        super(20,20,118,75, project);
        this.name = "";
        invalidate();
    }

    public Delay(double top, double left, double width, double height
            , String name, double delayValue, DelayUnit delayUnit, Project project) {
        // TODO: ignore width and height field to prevent line from drawing incorrectly when read file from old version as condition can't be resized anyway
        super(top, left, 118, 75, project);
        this.name = name;
        this.delayValue.set(delayValue);
        this.delayUnit.set(delayUnit);
        invalidate();
    }

    public Delay(Delay c, String name, Project project) {
        super(c.getTop(), c.getLeft(), c.getWidth(), c.getHeight(), project);
        this.name = name;
        this.delayValue.set(c.delayValue.get());
        this.delayUnit.set(c.delayUnit.get());
        invalidate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        invalidate();
        // invalidate other delay as every delay needs to check for duplicate name
        // note that name should be valid as it is not editable but we check anyway for safety
        for (Delay d : project.getUnmodifiableDelay()) {
            d.invalidate();
        }
    }

    public DelayUnit getDelayUnit() {
        return delayUnit.get();
    }

    public ObjectProperty<DelayUnit> delayUnitProperty() {
        return delayUnit;
    }

    public void setDelayUnit(DelayUnit unit) {
        delayUnit.set(unit);
    }

    public double getDelayValue() {
        return delayValue.get();
    }

    public DoubleProperty delayValueProperty() {
        return delayValue;
    }

    public void setDelayValue(double delay) {
        delayValue.set(delay);
    }

    @Override
    protected DiagramError checkError() {
        // name should contain only english alphabets and an underscore and it should not be empty
        if (!name.matches("\\w+")) {
            return DiagramError.DELAY_INVALID_NAME;
        }

        // name should be unique
        for (Delay d : project.getUnmodifiableDelay()) {
            if ((this != d) && name.equals(d.name)) {
                return DiagramError.DELAY_DUPLICATE_NAME;
            }
        }
        if (delayValue.get() < 0) {
            return DiagramError.DELAY_VALUE_INVALID;
        }
        if (delayUnit.get() == null) {
            return DiagramError.DELAY_UNIT_INVALID;
        }
        return DiagramError.NONE;
    }
}
