package io.makerplayground.project;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import java.util.List;

/**
 * Created by nuntipat on 6/2/2017 AD.
 */
public class DiagramVertex {
    private final SimpleStringProperty name;
    private final Point topLeft;
    private final SimpleDoubleProperty width, height;
    private final ObservableList<DeviceSettingPerState> activeDevice;
    private final ObservableList<DeviceSettingPerState> inactiveDevice;
    private final ObservableList<DeviceSettingPerState> unchangedDevice;
    private final SimpleDoubleProperty delayTime;
    private final Unit delayUnit;

    public DiagramVertex(String name) {
        this.name = new SimpleStringProperty(name);
        this.topLeft = new Point(20,20);
        this.width = new SimpleDoubleProperty(200);
        this.height = new SimpleDoubleProperty(300);
        this.activeDevice = FXCollections.observableArrayList();
        this.inactiveDevice = FXCollections.observableArrayList();
        this.unchangedDevice = FXCollections.observableArrayList();
        this.delayTime = new SimpleDoubleProperty(0);
        this.delayUnit = Unit.Second;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Point getTopLeft() {
        return topLeft;
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

    public ObservableList<DeviceSettingPerState> getActiveDevice() {
        return activeDevice;
    }

    public ObservableList<DeviceSettingPerState> getInactiveDevice() {
        return inactiveDevice;
    }

    public ObservableList<DeviceSettingPerState> getUnchangedDevice() {
        return unchangedDevice;
    }

    public double getDelayTime() {
        return delayTime.get();
    }

    public SimpleDoubleProperty delayTimeProperty() {
        return delayTime;
    }

    public void setDelayTime(double delayTime) {
        this.delayTime.set(delayTime);
    }

    public Unit getDelayUnit() {
        return delayUnit;
    }

    public enum Unit {MilliSecond, Second};
}
