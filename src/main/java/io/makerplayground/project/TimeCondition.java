package io.makerplayground.project;

import io.makerplayground.helper.TimeUnit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TimeCondition {
    private DoubleProperty duration;
    private ObjectProperty<TimeUnit> unit;
    private ObjectProperty<TimeConditionType> type;

    public TimeCondition() {
        this(0, TimeUnit.Second, TimeConditionType.AFTER);
    }

    public TimeCondition(double duration, TimeUnit unit, TimeConditionType type) {
        this.duration = new SimpleDoubleProperty(duration);
        this.unit = new SimpleObjectProperty<>(unit);
        this.type = new SimpleObjectProperty<>(type);
    }

    public double getDuration() {
        return duration.get();
    }

    public DoubleProperty durationProperty() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration.set(duration);
    }

    public TimeUnit getUnit() {
        return unit.get();
    }

    public ObjectProperty<TimeUnit> unitProperty() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit.set(unit);
    }

    public TimeConditionType getType() {
        return type.get();
    }

    public ObjectProperty<TimeConditionType> typeProperty() {
        return type;
    }

    public void setType(TimeConditionType type) {
        this.type.set(type);
    }
}
