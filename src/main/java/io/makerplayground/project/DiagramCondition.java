package io.makerplayground.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by nuntipat on 6/2/2017 AD.
 */
public class DiagramCondition {

    private final ObservableList<Point> edgePoint;
    private final ObservableList<DeviceSetting> activeInput;
    private final ObservableList<DeviceSetting> inactiveInput;

    public DiagramCondition() {
        this.edgePoint = FXCollections.observableArrayList();
        this.activeInput = FXCollections.observableArrayList();
        this.inactiveInput = FXCollections.observableArrayList();
    }

    public ObservableList<Point> getEdgePoint() {
        return edgePoint;
    }

    public ObservableList<DeviceSetting> getActiveInput() {
        return activeInput;
    }

    public ObservableList<DeviceSetting> getInactiveInput() {
        return inactiveInput;
    }
}
