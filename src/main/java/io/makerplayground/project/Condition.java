package io.makerplayground.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.shape.*;

/**
 * Created by nuntipat on 6/2/2017 AD.
 */
public class Condition {
    private final ObservableList<PathElement> waypoint;
    private final ObservableMap<ProjectDevice, StateDeviceSetting> setting;
    private final State sourceNode;
    private final State destNode;

    private final ObservableList<PathElement> unmodifiableWaypoint;
    private final ObservableMap<ProjectDevice, StateDeviceSetting> unmodifiableSetting;

    Condition(State sourceNode, State destNode) {
        this.sourceNode = sourceNode;
        this.destNode = destNode;
        waypoint = FXCollections.observableArrayList();
        unmodifiableWaypoint = FXCollections.unmodifiableObservableList(waypoint);

        recalculatePoint();

        sourceNode.getDestPort().xProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
        });

        sourceNode.getDestPort().yProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
        });

        destNode.getSourcePort().xProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
        });

        destNode.getSourcePort().yProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
        });

        setting = FXCollections.observableHashMap();
        unmodifiableSetting = FXCollections.unmodifiableObservableMap(setting);
    }

    private void recalculatePoint() {
        MoveTo moveTo = new MoveTo();
        moveTo.setX(sourceNode.getDestPort().getX());
        moveTo.setY(sourceNode.getDestPort().getY());

        CubicCurveTo cubicCurveTo = new CubicCurveTo();
        cubicCurveTo.setControlX1(sourceNode.getDestPort().getX()*0.25 + destNode.getSourcePort().getX()*0.75);
        cubicCurveTo.setControlY1(sourceNode.getDestPort().getY());
        cubicCurveTo.setControlX2(destNode.getSourcePort().getX()*0.25 + sourceNode.getDestPort().getX()*0.75);
        cubicCurveTo.setControlY2(destNode.getSourcePort().getY());
        cubicCurveTo.setX(destNode.getSourcePort().getX());
        cubicCurveTo.setY(destNode.getSourcePort().getY());
        waypoint.setAll(moveTo,cubicCurveTo);
    }

    public State getSourceNode() {
        return sourceNode;
    }

    public State getDestNode() {
        return destNode;
    }

    public ObservableList<PathElement> getUnmodifiableWaypoint() {
        return unmodifiableWaypoint;
    }

    public ObservableMap<ProjectDevice, StateDeviceSetting> getUnmodifiableSetting() {
        return unmodifiableSetting;
    }
}
