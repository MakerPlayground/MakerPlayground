package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.node.SceneDeviceIconViewModel;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionViewModel {
    private final Condition condition;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public ConditionViewModel(Condition condition, Project project) {
        this.condition = condition;
        this.project = project;

         this.dynamicViewModelCreator = new DynamicViewModelCreator<>(condition.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, condition, project));

        hasDeviceToAdd = new SimpleBooleanProperty(condition.getSetting().size() != project.getInputDevice().size());
        // TODO: find a better way (now since only sensor and connectivity can be in the condition so we track these two)
        this.project.getSensor().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(condition.getSetting().size() != project.getInputDevice().size());
        });
        this.project.getConnectivity().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(condition.getSetting().size() != project.getInputDevice().size());
        });
        // when we remove something from the condition
        this.condition.getSetting().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(condition.getSetting().size() != project.getInputDevice().size());
        });
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() { return condition.getLeft(); }

    public DoubleProperty xProperty() { return condition.leftProperty();}

    public double getY() { return condition.getTop(); }

    public DoubleProperty yProperty() { return condition.topProperty(); }

    public List<ProjectDevice> getProjectInputDevice() {
        return project.getInputDevice();
    }

    public Condition getCondition() { return condition; }

    public ObservableList<UserSetting> getConditionDevice() { return condition.getSetting(); }

    public void removeConditionDevice(ProjectDevice projectDevice) { condition.removeDevice(projectDevice); }

    public BooleanProperty hasDeviceToAddProperty() { return hasDeviceToAdd; }

    public boolean hasConnectionFrom(NodeElement other) {
        return project.hasLine(other, condition);
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(condition, other);
    }

    public boolean isError() {
        return condition.isError();
    }
}
