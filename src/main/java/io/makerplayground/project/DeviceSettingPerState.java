package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;

import java.util.Map;

/**
 * Created by tanyagorn on 6/9/2017.
 */
public class DeviceSettingPerState {
    private ProjectDevice device;
    private Action action;
    private Map<Parameter, Object> valueMap;

    public DeviceSettingPerState(ProjectDevice device) {
        this.device = device;
        //Device d = DeviceLibrary.INSTANCE.getOutputDevice("led");
    }

    public ProjectDevice getDevice() {
        return device;
    }

//    public <T> T getParameterValue(Parameter p, Class<T> type) {
//        return type.cast(valueMap.get(p));
//    }
}
