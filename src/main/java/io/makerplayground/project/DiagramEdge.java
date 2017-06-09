package io.makerplayground.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by nuntipat on 6/2/2017 AD.
 */
public class DiagramEdge {

    private final ObservableList<Point> edgePoint;
    private final ObservableList<DeviceSettingPerState> activeInput;
    private final ObservableList<DeviceSettingPerState> inactiveInput;

    public DiagramEdge() {
        this.edgePoint = FXCollections.observableArrayList();
        this.activeInput = FXCollections.observableArrayList();
        this.inactiveInput = FXCollections.observableArrayList();
    }

    public ObservableList<Point> getEdgePoint() {
        return edgePoint;
    }

    public ObservableList<DeviceSettingPerState> getActiveInput() {
        return activeInput;
    }

    public ObservableList<DeviceSettingPerState> getInactiveInput() {
        return inactiveInput;
    }
}
