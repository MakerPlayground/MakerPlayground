package io.makerplayground.project;

import javafx.beans.property.SimpleDoubleProperty;

/**
 * Created by tanyagorn on 6/9/2017.
 */
public class Point {
    private SimpleDoubleProperty x;
    private SimpleDoubleProperty y;

    public Point(double x, double y) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    public double getX() {
        return x.get();
    }

    public SimpleDoubleProperty xProperty() {
        return x;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public SimpleDoubleProperty yProperty() {
        return y;
    }

    public void setY(double y) {
        this.y.set(y);
    }

}
