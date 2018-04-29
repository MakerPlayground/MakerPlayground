package io.makerplayground.ui.canvas;

import io.makerplayground.project.Begin;
import io.makerplayground.project.Line;
import io.makerplayground.project.NodeElement;
import io.makerplayground.project.Project;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class BeginSceneViewModel {
    private final Begin begin;
    private final Project project;

    public BeginSceneViewModel(Begin begin, Project project) {
        this.begin = begin;
        this.project = project;

    }

    public Begin getBegin() {
        return begin;
    }

    public double getX() {
        return begin.getLeft();
    }

    public DoubleProperty xProperty() {
        return begin.leftProperty();
    }

    public double getY() {
        return begin.getTop();
    }

    public DoubleProperty yProperty() {
        return begin.topProperty();
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(begin, other);
    }

    public boolean isError() {
        return false;
        // TODO: begin node doesn't get update when other disconnect it
        //return !project.hasLineFrom(begin);
    }
}
