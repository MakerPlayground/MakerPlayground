package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * Created by tanyagorn on 6/9/2017.
 */
public class UserSetting {
    private final ProjectDevice device;
    private ObjectProperty<Action> action;
    private final ObservableMap<Parameter, Object> valueMap;

    UserSetting(ProjectDevice device) {
        this.device = device;
        this.action = new SimpleObjectProperty<>(device.getGenericDevice().getDefaultAction());
        this.valueMap = FXCollections.observableHashMap();
        // Initialize the map with default value of each parameter
        for (Parameter param : this.action.get().getParameter()) {
            this.valueMap.put(param, param.getDefaultValue());
        }

        this.action.addListener(new ChangeListener<Action>() {
            @Override
            public void changed(ObservableValue<? extends Action> observable, Action oldValue, Action newValue) {
                valueMap.clear();
                for (Parameter param : action.get().getParameter()) {
                    valueMap.put(param, param.getDefaultValue());
                }

            }
        });
    }

    public ProjectDevice getDevice() {
        return device;
    }

    public Action getAction() {
        return action.get();
    }

    public ObjectProperty<Action> actionProperty() {
        return action;
    }

    public void setAction(Action action) {
        this.action.set(action);
    }

    public ObservableMap<Parameter, Object> getValueMap() {
        return valueMap;
    }
}
