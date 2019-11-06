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

@JsonSerialize(using = NodeElementSerializer.class)
public abstract class NodeElement {
    protected final Project project;

    private final SimpleDoubleProperty top;
    private final SimpleDoubleProperty left;
    private final SimpleDoubleProperty width;
    private final SimpleDoubleProperty height;

    private final DoubleProperty sourcePortX;
    private final DoubleProperty sourcePortY;
    private final DoubleProperty destPortX;
    private final DoubleProperty destPortY;

    private final ReadOnlyObjectWrapper<DiagramError> error;

    protected NodeElement(double top,double left,double width, double height, Project project) {
        this.project = project;

        this.top = new SimpleDoubleProperty(top);
        this.left = new SimpleDoubleProperty(left);
        this.width = new SimpleDoubleProperty(width);
        this.height = new SimpleDoubleProperty(height);

        this.sourcePortX = new SimpleDoubleProperty();
        this.sourcePortY = new SimpleDoubleProperty();
        this.destPortX = new SimpleDoubleProperty();
        this.destPortY = new SimpleDoubleProperty();

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

    public final DoubleProperty sourcePortXProperty() {
        return sourcePortX;
    }

    public final double getSourcePortY() {
        return sourcePortY.get();
    }

    public final DoubleProperty sourcePortYProperty() {
        return sourcePortY;
    }

    public final double getDestPortX() {
        return destPortX.get();
    }

    public final DoubleProperty destPortXProperty() {
        return destPortX;
    }

    public final double getDestPortY() {
        return destPortY.get();
    }

    public final DoubleProperty destPortYProperty() {
        return destPortY;
    }

    public final DiagramError getError() {
        return error.get();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return error.getReadOnlyProperty();
    }

    protected abstract DiagramError checkError();

    public final void invalidate() {
        this.project.calculateCompatibility();
        error.set(checkError());
    }
}
