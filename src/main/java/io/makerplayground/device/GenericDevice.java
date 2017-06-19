package io.makerplayground.device;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class GenericDevice {
    private final String name;
    private final List<Action> action;
    private final List<Value> value;

    public GenericDevice(String name, List<Action> action, List<Value> value) {
        this.name = name;
        this.action = Collections.unmodifiableList(action);
        this.value = Collections.unmodifiableList(value);
    }

    public String getName() {
        return name;
    }

    public List<Action> getAction() {
        return action;
    }

    public List<Value> getValue() {
        return value;
    }
}
