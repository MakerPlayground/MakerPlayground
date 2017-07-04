package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import javafx.collections.ObservableList;
import javafx.scene.shape.PathElement;

/**
 * Created by USER on 20-Jun-17.
 */
public class LineViewModel {
    private final Condition condition;

    public LineViewModel(Condition condition) {
        this.condition = condition;
    }

    public ObservableList<PathElement> getPoint() {
        return condition.getUnmodifiableWaypoint();
    }
}
