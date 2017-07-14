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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 */
@JsonSerialize(using = NodeElementSerializer.class)
public class NodeElement {
    private static final double PORT_RADIUS = 15;

    private final SimpleDoubleProperty top;
    private final SimpleDoubleProperty left;
    private final SimpleDoubleProperty width;
    private final SimpleDoubleProperty height;

    private final ReadOnlyDoubleWrapper sourcePortX;
    private final ReadOnlyDoubleWrapper sourcePortY;
    private final ReadOnlyDoubleWrapper destPortX;
    private final ReadOnlyDoubleWrapper destPortY;

    NodeElement(double top,double left,double width, double height) {
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
    }

    public double getTop() {
        return top.get();
    }

    public SimpleDoubleProperty topProperty() {
        return top;
    }

    public void setTop(double top) {
        this.top.set(top);
    }

    public double getLeft() {
        return left.get();
    }

    public SimpleDoubleProperty leftProperty() {
        return left;
    }

    public void setLeft(double left) {
        this.left.set(left);
    }

    public double getWidth() {
        return width.get();
    }

    public SimpleDoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public SimpleDoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public double getSourcePortX() {
        return sourcePortX.get();
    }

    public ReadOnlyDoubleProperty sourcePortXProperty() {
        return sourcePortX.getReadOnlyProperty();
    }

    public double getSourcePortY() {
        return sourcePortY.get();
    }

    public ReadOnlyDoubleProperty sourcePortYProperty() {
        return sourcePortY.getReadOnlyProperty();
    }

    public double getDestPortX() {
        return destPortX.get();
    }

    public ReadOnlyDoubleProperty destPortXProperty() {
        return destPortX.getReadOnlyProperty();
    }

    public double getDestPortY() {
        return destPortY.get();
    }

    public ReadOnlyDoubleProperty destPortYProperty() {
        return destPortY.getReadOnlyProperty();
    }
}
