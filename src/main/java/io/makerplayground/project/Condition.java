package io.makerplayground.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * Created by nuntipat on 6/2/2017 AD.
 */
public class Condition {
    private final ObservableList<Double> waypoint;
    private final ObservableMap<ProjectDevice, UserSetting> setting;

    private final ObservableList<Double> unmodifiableWaypoint;
    private final ObservableMap<ProjectDevice, UserSetting> unmodifiableSetting;

    Condition() {
        waypoint = FXCollections.observableArrayList();
        unmodifiableWaypoint = FXCollections.unmodifiableObservableList(waypoint);

        setting = FXCollections.observableHashMap();
        unmodifiableSetting = FXCollections.unmodifiableObservableMap(setting);
    }

    public ObservableList<Double> getUnmodifiableWaypoint() {
        return unmodifiableWaypoint;
    }

    public ObservableMap<ProjectDevice, UserSetting> getUnmodifiableSetting() {
        return unmodifiableSetting;
    }
}
