package io.makerplayground.project;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * Created by tanyagorn on 6/2/2017 AD.
 */
public class DiagramState {

    public enum DelayUnit {MilliSecond, Second};
    private String name;
    private final Point topLeft;
    private final SimpleDoubleProperty width, height;
    private final ObservableList<DeviceSetting> deviceSetting;
    private final SimpleDoubleProperty delayDuration;
    private final DelayUnit delayUnit;

    DiagramState(String name) {
        this.name = name;
        this.topLeft = new Point(20, 20);
        this.width = new SimpleDoubleProperty(200);
        this.height = new SimpleDoubleProperty(300);
        this.deviceSetting = FXCollections.observableArrayList();
        this.unmodifiableDeviceSetting = FXCollections.unmodifiableObservableList(this.deviceSetting);
        this.delayDuration = new SimpleDoubleProperty(0);
        this.delayUnit = DelayUnit.Second;
    }

    public void removeDevice(ProjectDevice device) {
        for (int i=deviceSetting.size()-1; i>=0; i--) {
            DeviceSetting eachDevice = deviceSetting.get(i);
            if (eachDevice.getDevice() == device) {
                deviceSetting.remove(eachDevice);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    ObservableList<DeviceSetting> getDeviceSetting() {
        return deviceSetting;
    }

    private final ObservableList<DeviceSetting> unmodifiableDeviceSetting;

    public ObservableList<DeviceSetting> getUnmodifiableDeviceSetting() {
        return unmodifiableDeviceSetting;
    }

    public double getDelayDuration() {
        return delayDuration.get();
    }

    public SimpleDoubleProperty delayDurationProperty() {
        return delayDuration;
    }

    public void setDelayDuration(double delayDuration) {
        this.delayDuration.set(delayDuration);
    }

    public DelayUnit getDelayUnit() {
        return delayUnit;
    }
}
