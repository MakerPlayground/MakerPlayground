package io.makerplayground.ui;

import io.makerplayground.project.Condition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by USER on 20-Jun-17.
 */
public class LineViewModel {
    //private final ObservableList<Double> point;
    private final Condition condition;

    public LineViewModel(Condition condition) {
        this.condition = condition;
        //this.point = FXCollections.observableArrayList();
    }

    public ObservableList<Double> getPoint() {
        //return point;
        return condition.getUnmodifiableWaypoint();
    }
}
