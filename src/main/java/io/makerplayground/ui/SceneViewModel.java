package io.makerplayground.ui;

import io.makerplayground.device.ActionType;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.State;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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

    private final DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> dynamicViewModelCreator;

    public SceneViewModel(State state, Project project) {
        this.state = state;
        this.name = new SimpleStringProperty(state.getName());
        this.delay = new SimpleDoubleProperty(state.getDelay());
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(state.getSetting(), StateDeviceIconViewModel::new);

        this.devicePropertyWindow = null;
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

    public DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> getDynamicViewModelCreator() {
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

    public ObservableList<UserSetting> getStateDevice() {
        return state.getSetting();
    }

    public void removeStateDevice(ProjectDevice projectDevice) {
        state.removeDevice(projectDevice);
    }
}
