package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by tanyagorn on 6/9/2017.
 */
public class UserSetting {
    private final ProjectDevice device;
    private Action action;
    private final Map<Parameter, Object> valueMap;

    UserSetting(ProjectDevice device) {
        this.device = device;
        this.action = device.getGenericDevice().getAction().get(0);
        this.valueMap = new HashMap<>();
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

    public Object getParameterValue(Parameter p) {
        return valueMap.get(p);
    }
}
