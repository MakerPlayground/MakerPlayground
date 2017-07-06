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

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 */
public class Condition {
    private final ObservableList<UserSetting> setting;
    private final ObjectProperty<State> sourceNode;
    private final ObjectProperty<State> destNode;
    private final SimpleDoubleProperty top, left, width, height;
    private final ObservableList<UserSetting> unmodifiableSetting;

    private final ReadOnlyDoubleWrapper sourcePortX, sourcePortY;
    private final ReadOnlyDoubleWrapper destPortX, destPortY;

    Condition() {
        this.setting = FXCollections.observableArrayList();
        this.sourceNode = new SimpleObjectProperty<>(null);
        this.destNode = new SimpleObjectProperty<>(null);
        this.top = new SimpleDoubleProperty(0);
        this.left = new SimpleDoubleProperty(0);
        this.width = new SimpleDoubleProperty(200);
        this.height = new SimpleDoubleProperty(100);
        this.sourcePortX = new ReadOnlyDoubleWrapper();
        this.sourcePortY = new ReadOnlyDoubleWrapper();
        this.destPortX = new ReadOnlyDoubleWrapper();
        this.destPortY = new ReadOnlyDoubleWrapper();
        this.sourcePortX.bind(left);
        this.sourcePortY.bind(top.add(height.divide(2)));
        this.destPortX.bind(left.add(width).add(30));
        this.destPortY.bind(top.add(height.divide(2)));
        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
    }

    public State getSourceNode() {
        return sourceNode.get();
    }

    public ObjectProperty<State> sourceNodeProperty() {
        return sourceNode;
    }

    public State getDestNode() {
        return destNode.get();
    }

    public ObjectProperty<State> destNodeProperty() {
        return destNode;
    }

    public void addDevice(ProjectDevice device) {
        setting.add(new UserSetting(device));
    }

    public void removeDevice(ProjectDevice device) {
        setting.remove(device);
    }

    public ObservableList<UserSetting> getUnmodifiableSetting() {
        return unmodifiableSetting;
    }

    public ObservableList<UserSetting> getSetting() { return setting; }

    public double getTop() { return top.get(); }

    public SimpleDoubleProperty topProperty() { return top; }

    public double getLeft() { return left.get(); }

    public SimpleDoubleProperty leftProperty() { return left; }

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

