package io.makerplayground.ui.canvas;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.Scene;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class SceneViewModel {
    private final Scene scene;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public SceneViewModel(Scene scene, Project project) {
        this.scene = scene;
        this.name = new SimpleStringProperty(scene.getName());
        this.delay = new SimpleDoubleProperty(scene.getDelay());
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(scene.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, project));

        hasDeviceToAdd = new SimpleBooleanProperty(scene.getSetting().size() != project.getOutputDevice().size());
        this.project.getOutputDevice().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(scene.getSetting().size() != project.getOutputDevice().size());
        });
        this.scene.getSetting().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(scene.getSetting().size() != project.getOutputDevice().size());
        });
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public double getDelay() {
        return delay.get();
    }

    public SimpleDoubleProperty delayProperty() {
        return delay;
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() {
        return scene.getLeft();
    }

    public DoubleProperty xProperty() {
        return scene.leftProperty();
    }

    public double getY() {
        return scene.getTop();
    }

    public DoubleProperty yProperty() {
        return scene.topProperty();
    }

    public ObservableList<ProjectDevice> getProjectOutputDevice() {
        return project.getOutputDevice();
    }

    public Scene getScene() {
        return scene;
    }

    public ObservableList<UserSetting> getStateDevice() {
        return scene.getSetting();
    }

    public void removeStateDevice(ProjectDevice projectDevice) {
        scene.removeDevice(projectDevice);
    }

    public BooleanProperty hasDeviceToAddProperty() {
        return hasDeviceToAdd;
    }
}
