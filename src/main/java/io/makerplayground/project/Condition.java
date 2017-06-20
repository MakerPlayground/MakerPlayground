package io.makerplayground.project;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Created by nuntipat on 6/2/2017 AD.
 */
public class Condition {
    private final ObservableList<Double> waypoint;
    private final ObservableMap<ProjectDevice, UserSetting> setting;
    private final State sourceNode, destNode;

    private final ObservableList<Double> unmodifiableWaypoint;
    private final ObservableMap<ProjectDevice, UserSetting> unmodifiableSetting;

    Condition(State sourceNode, State destNode) {
        this.sourceNode = sourceNode;
        this.destNode = destNode;

        waypoint = FXCollections.observableArrayList();
        unmodifiableWaypoint = FXCollections.unmodifiableObservableList(waypoint);

        waypoint.addAll(sourceNode.getDestPort().getX(), sourceNode.getDestPort().getY(), destNode.getSourcePort().getX(), destNode.getSourcePort().getY());
        sourceNode.getDestPort().xProperty().addListener((observable, oldValue, newValue) -> waypoint.set(0, newValue.doubleValue()));
        sourceNode.getDestPort().yProperty().addListener((observable, oldValue, newValue) -> waypoint.set(1, newValue.doubleValue()));
        destNode.getSourcePort().xProperty().addListener((observable, oldValue, newValue) -> waypoint.set(2, newValue.doubleValue()));
        destNode.getSourcePort().yProperty().addListener((observable, oldValue, newValue) -> waypoint.set(3, newValue.doubleValue()));


        setting = FXCollections.observableHashMap();
        unmodifiableSetting = FXCollections.unmodifiableObservableMap(setting);
    }

    public State getSourceNode() {
        return sourceNode;
    }

    public State getDestNode() {
        return destNode;
    }

    public ObservableList<Double> getUnmodifiableWaypoint() {
        return unmodifiableWaypoint;
    }

    public ObservableMap<ProjectDevice, UserSetting> getUnmodifiableSetting() {
        return unmodifiableSetting;
    }
}
