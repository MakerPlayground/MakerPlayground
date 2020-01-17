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

package io.makerplayground.ui.canvas.node;

import io.makerplayground.device.shared.DelayUnit;
import io.makerplayground.project.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class DelayViewModel {
    private final Delay delay;
    private final Project project;

    private final BooleanProperty hasLineIn;
    private final BooleanProperty hasLineOut;

    public DelayViewModel(Delay delay, Project project) {
        this.delay = delay;
        this.project = project;

        hasLineIn = new SimpleBooleanProperty();
        hasLineIn.bind(Bindings.size(project.getUnmodifiableLine().filtered(line -> line.getDestination() == delay)).greaterThan(0));

        hasLineOut = new SimpleBooleanProperty();
        hasLineOut.bind(Bindings.size(project.getUnmodifiableLine().filtered(line -> line.getSource() == delay)).greaterThan(0));
    }

    public Delay getDelay() {
        return delay;
    }

    public DelayUnit getDelayUnit() {
        return delay.getDelayUnit();
    }

    public ObjectProperty<DelayUnit> delayUnitProperty() {
        return delay.delayUnitProperty();
    }

    public double getDelayValue() {
        return delay.getDelayValue();
    }

    public void setDelayValue(double d) {
        delay.setDelayValue(d);
    }

    public DoubleProperty delayValueProperty() {
        return delay.delayValueProperty();
    }

    public double getX() {
        return delay.getLeft();
    }

    public DoubleProperty xProperty() {
        return delay.leftProperty();
    }

    public double getY() {
        return delay.getTop();
    }

    public DoubleProperty yProperty() {
        return delay.topProperty();
    }

    public double getSourcePortX() {
        return delay.getSourcePortX();
    }

    public DoubleProperty sourcePortXProperty() {
        return delay.sourcePortXProperty();
    }

    public double getSourcePortY() {
        return delay.getSourcePortY();
    }

    public DoubleProperty sourcePortYProperty() {
        return delay.sourcePortYProperty();
    }

    public double getDestPortX() {
        return delay.getDestPortX();
    }

    public DoubleProperty destPortXProperty() {
        return delay.destPortXProperty();
    }

    public double getDestPortY() {
        return delay.getDestPortY();
    }

    public DoubleProperty destPortYProperty() {
        return delay.destPortYProperty();
    }

    public ReadOnlyBooleanProperty hasLineInProperty() {
        return hasLineIn;
    }

    public ReadOnlyBooleanProperty hasLineOutProperty() {
        return hasLineOut;
    }

    public boolean hasConnectionFrom(NodeElement other) {
        return project.hasLine(other, delay);
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(delay, other);
    }

    public final DiagramError getError() {
        return delay.getError();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return delay.errorProperty();
    }
}
