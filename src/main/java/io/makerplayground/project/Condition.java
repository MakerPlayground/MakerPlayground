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
    private final State sourceNode;
    private final State destNode;
//    private final State midFirstNode;
//    private final State midSecondNode;

    private final ObservableList<Double> unmodifiableWaypoint;
    private final ObservableMap<ProjectDevice, UserSetting> unmodifiableSetting;

    Condition(State sourceNode, State destNode) {
        this.sourceNode = sourceNode;
        this.destNode = destNode;


        waypoint = FXCollections.observableArrayList();
        unmodifiableWaypoint = FXCollections.unmodifiableObservableList(waypoint);

//      x2 > x1
        if (destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
            waypoint.addAll(sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , sourceNode.getDestPort().getY()
                    , (destNode.getSourcePort().getX() - sourceNode.getDestPort().getX()) / 2 + sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , (destNode.getSourcePort().getX() - sourceNode.getDestPort().getX()) / 2 + sourceNode.getDestPort().getX()
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX() - 20.0
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX()
                    , destNode.getSourcePort().getY());
        } else {
            waypoint.addAll(sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , (destNode.getSourcePort().getY() + sourceNode.getDestPort().getY()) / 2
                    , destNode.getSourcePort().getX() - 20.0
                    , (destNode.getSourcePort().getY() + sourceNode.getDestPort().getY()) / 2
                    , destNode.getSourcePort().getX() - 20.0
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX()
                    , destNode.getSourcePort().getY());
        }

        sourceNode.getDestPort().xProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
//            if (destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
//                waypoint.set(0, newValue.doubleValue());
//                waypoint.set(2, newValue.doubleValue() + 20.0);
//                waypoint.set(4, (destNode.getSourcePort().getX() + newValue.doubleValue()) / 2);
//                waypoint.set(6, (destNode.getSourcePort().getX() + newValue.doubleValue()) / 2);
//            } else {
//                waypoint.set(0, newValue.doubleValue());
//                waypoint.set(2, newValue.doubleValue() + 20.0);
//                waypoint.set(4, newValue.doubleValue() + 20.0);
//            }
        });
        sourceNode.getDestPort().yProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
//            if (destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
//                waypoint.set(1, newValue.doubleValue());
//                waypoint.set(3, newValue.doubleValue());
//                waypoint.set(5, newValue.doubleValue());
//            } else {
//                waypoint.set(1, newValue.doubleValue());
//                waypoint.set(3, newValue.doubleValue());
//                waypoint.set(5, (destNode.getSourcePort().getY() + newValue.doubleValue()) / 2);
//                waypoint.set(7, (destNode.getSourcePort().getY() + newValue.doubleValue()) / 2);
//            }
        });
        destNode.getSourcePort().xProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
//            if (destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
//                waypoint.set(4, (newValue.doubleValue() + sourceNode.getDestPort().getX()) / 2);
//                waypoint.set(6, (newValue.doubleValue() + sourceNode.getDestPort().getX()) / 2);
//                waypoint.set(8, newValue.doubleValue() - 20.0);
//                waypoint.set(10, newValue.doubleValue());
//            } else {
//                waypoint.set(6, newValue.doubleValue() - 20.0);
//                waypoint.set(8, newValue.doubleValue() - 20.0);
//                waypoint.set(10, newValue.doubleValue());
//            }
        });
        destNode.getSourcePort().yProperty().addListener((observable, oldValue, newValue) -> {
            recalculatePoint();
//            if(destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
//                waypoint.set(7 , newValue.doubleValue());
//                waypoint.set(9 , newValue.doubleValue());
//                waypoint.set(11 , newValue.doubleValue());
//            } else {
//                waypoint.set(5 , (newValue.doubleValue() + sourceNode.getDestPort().getY()) / 2);
//                waypoint.set(7 , (newValue.doubleValue() + sourceNode.getDestPort().getY()) / 2);
//                waypoint.set(9 , newValue.doubleValue());
//                waypoint.set(11 , newValue.doubleValue());
//            }
        });

        setting = FXCollections.observableHashMap();
        unmodifiableSetting = FXCollections.unmodifiableObservableMap(setting);
    }

    private void recalculatePoint() {
        if (destNode.getSourcePort().getX() - 20.0 > sourceNode.getDestPort().getX() + 20.0) {
            waypoint.setAll(sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , sourceNode.getDestPort().getY()
                    , (destNode.getSourcePort().getX() - sourceNode.getDestPort().getX()) / 2 + sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , (destNode.getSourcePort().getX() - sourceNode.getDestPort().getX()) / 2 + sourceNode.getDestPort().getX()
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX() - 20.0
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX()
                    , destNode.getSourcePort().getY());
        } else {
            waypoint.setAll(sourceNode.getDestPort().getX()
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , sourceNode.getDestPort().getY()
                    , sourceNode.getDestPort().getX() + 20.0
                    , (destNode.getSourcePort().getY() + sourceNode.getDestPort().getY()) / 2
                    , destNode.getSourcePort().getX() - 20.0
                    , (destNode.getSourcePort().getY() + sourceNode.getDestPort().getY()) / 2
                    , destNode.getSourcePort().getX() - 20.0
                    , destNode.getSourcePort().getY()
                    , destNode.getSourcePort().getX()
                    , destNode.getSourcePort().getY());
        }
    }

    public State getSourceNode() {
        return sourceNode;
    }

    public State getDestNode() {
        return destNode;
    }

//    public State getMidFirstNode() { return midFirstNode; }
//
//    public State getMidSecondNode() { return midSecondNode; }

    public ObservableList<Double> getUnmodifiableWaypoint() {
        return unmodifiableWaypoint;
    }

    public ObservableMap<ProjectDevice, UserSetting> getUnmodifiableSetting() {
        return unmodifiableSetting;
    }
}
