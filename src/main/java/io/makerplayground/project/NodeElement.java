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
import javafx.beans.property.*;

/**
 *
 */
@JsonSerialize(using = NodeElementSerializer.class)
public abstract class NodeElement {
    private static final double PORT_RADIUS = 15;

    protected final Project project;

    private final SimpleDoubleProperty top;
    private final SimpleDoubleProperty left;
    private final SimpleDoubleProperty width;
    private final SimpleDoubleProperty height;

    private final ReadOnlyDoubleWrapper sourcePortX;
    private final ReadOnlyDoubleWrapper sourcePortY;
    private final ReadOnlyDoubleWrapper destPortX;
    private final ReadOnlyDoubleWrapper destPortY;

    private final ReadOnlyObjectWrapper<DiagramError> error;

    protected NodeElement(double top,double left,double width, double height, Project project) {
        this.project = project;

        this.top = new SimpleDoubleProperty(top);
        this.left = new SimpleDoubleProperty(left);
        this.width = new SimpleDoubleProperty(width);
        this.height = new SimpleDoubleProperty(height);

        this.sourcePortX = new ReadOnlyDoubleWrapper();
        this.sourcePortY = new ReadOnlyDoubleWrapper();
        this.destPortX = new ReadOnlyDoubleWrapper();
        this.destPortY = new ReadOnlyDoubleWrapper();

        this.sourcePortX.bind(this.left.add(5));
        this.sourcePortY.bind(this.top.add(this.height.divide(2.0)));
        this.destPortX.bind(this.left.add(this.width).add(PORT_RADIUS));
        this.destPortY.bind(this.top.add(this.height.divide(2.0)));

        this.error = new ReadOnlyObjectWrapper<>(DiagramError.NONE);
    }

    public final double getTop() {
        return top.get();
    }

    public final DoubleProperty topProperty() {
        return top;
    }

    public final void setTop(double top) {
        this.top.set(top);
    }

    public final double getLeft() {
        return left.get();
    }

    public final DoubleProperty leftProperty() {
        return left;
    }

    public final void setLeft(double left) {
        this.left.set(left);
    }

    public final double getWidth() {
        return width.get();
    }

    public final DoubleProperty widthProperty() {
        return width;
    }

    public final void setWidth(double width) {
        this.width.set(width);
    }

    public final double getHeight() {
        return height.get();
    }

    public final DoubleProperty heightProperty() {
        return height;
    }

    public final void setHeight(double height) {
        this.height.set(height);
    }

    public final double getSourcePortX() {
        return sourcePortX.get();
    }

    public final ReadOnlyDoubleProperty sourcePortXProperty() {
        return sourcePortX.getReadOnlyProperty();
    }

    public final double getSourcePortY() {
        return sourcePortY.get();
    }

    public final ReadOnlyDoubleProperty sourcePortYProperty() {
        return sourcePortY.getReadOnlyProperty();
    }

    public final double getDestPortX() {
        return destPortX.get();
    }

    public final ReadOnlyDoubleProperty destPortXProperty() {
        return destPortX.getReadOnlyProperty();
    }

    public final double getDestPortY() {
        return destPortY.get();
    }

    public final ReadOnlyDoubleProperty destPortYProperty() {
        return destPortY.getReadOnlyProperty();
    }

    public final DiagramError getError() {
        return error.get();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return error.getReadOnlyProperty();
    }

    protected abstract DiagramError checkError();

    public final void invalidate() {
        error.set(checkError());
    }
}
