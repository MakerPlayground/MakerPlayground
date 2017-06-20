package io.makerplayground.device;

import java.util.Collections;
import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
public class Device {
    private final String brand;
    private final String model;
    private final String url;
    private final Map<Action, Map<Parameter, Constraint>> supportedAction;
    private final Map<Value, Constraint>  supportedValue;

    public Device(String brand, String model, String url, Map<Action, Map<Parameter, Constraint>> supportedAction, Map<Value, Constraint> supportedValue) {
        this.brand = brand;
        this.model = model;
        this.url = url;
        this.supportedAction = Collections.unmodifiableMap(supportedAction);
        this.supportedValue = Collections.unmodifiableMap(supportedValue);
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getUrl() {
        return url;
    }

    public Map<Action, Map<Parameter, Constraint>> getSupportedAction() {
        return supportedAction;
    }

    public Map<Value, Constraint> getSupportedValue() {
        return supportedValue;
    }
}
