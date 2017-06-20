package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * Created by tanyagorn on 6/9/2017.
 */
public class UserSetting {
    private final ProjectDevice device;
    private Action action;
    private final ObservableMap<Parameter, Object> valueMap;

    UserSetting(ProjectDevice device) {
        this.device = device;
        this.action = device.getGenericDevice().getDefaultAction();
        this.valueMap = FXCollections.observableHashMap();
        // Initialize the map with default value of each parameter
        for (Parameter param : this.action.getParameter()) {
            this.valueMap.put(param, param.getDefaultValue());
        }
    }

    public ProjectDevice getDevice() {
        return device;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ObservableMap<Parameter, Object> getValueMap() {
        return valueMap;
    }
}
