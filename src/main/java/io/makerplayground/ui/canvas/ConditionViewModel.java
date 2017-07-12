package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

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

         this.dynamicViewModelCreator = new DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel>(condition.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, project));

         hasDeviceToAdd = new SimpleBooleanProperty(condition.getSetting().size() != project.getInputDevice().size());
         this.project.getInputDevice().addListener((InvalidationListener) observable -> {
             hasDeviceToAdd.set(condition.getSetting().size() != project.getInputDevice().size());
         });
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

    public ObservableList<ProjectDevice> getProjectInputDevice() { return project.getInputDevice(); }

    public Condition getCondition() { return condition; }

    public ObservableList<UserSetting> getConditionDevice() { return condition.getSetting(); }

    public void removeConditionDevice(ProjectDevice projectDevice) { condition.removeDevice(projectDevice); }

    public BooleanProperty hasDeviceToAddProperty() { return hasDeviceToAdd; }

}
