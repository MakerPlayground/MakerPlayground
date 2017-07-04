package io.makerplayground.ui.canvas;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.State;
import io.makerplayground.project.StateDeviceSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class SceneViewModel {
    private final State state;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    private final Project project;

    DevicePropertyWindow devicePropertyWindow;

    private final DynamicViewModelCreator<StateDeviceSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public SceneViewModel(State state, Project project) {
        this.state = state;
        this.name = new SimpleStringProperty(state.getName());
        this.delay = new SimpleDoubleProperty(state.getDelay());
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(state.getSetting(), SceneDeviceIconViewModel::new);

        this.devicePropertyWindow = null;

        hasDeviceToAdd = new SimpleBooleanProperty(state.getSetting().size() != project.getOutputDevice().size());
        this.project.getOutputDevice().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(state.getSetting().size() != project.getOutputDevice().size());
        });
        this.state.getSetting().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(state.getSetting().size() != project.getOutputDevice().size());
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

    public DynamicViewModelCreator<StateDeviceSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() {
        return state.getPosition().getX();
    }

    public DoubleProperty xProperty() {
        return state.getPosition().xProperty();
    }

    public double getY() {
        return state.getPosition().getY();
    }

    public DoubleProperty yProperty() {
        return state.getPosition().yProperty();
    }

    public ObservableList<ProjectDevice> getProjectOutputDevice() {
        return project.getOutputDevice();
    }

    public State getState() {
        return state;
    }

    public ObservableList<StateDeviceSetting> getStateDevice() {
        return state.getSetting();
    }

    public void removeStateDevice(ProjectDevice projectDevice) {
        state.removeDevice(projectDevice);
    }

    public BooleanProperty hasDeviceToAddProperty() {
        return hasDeviceToAdd;
    }
}
