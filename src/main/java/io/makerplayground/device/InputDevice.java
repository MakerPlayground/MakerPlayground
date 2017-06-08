package io.makerplayground.device;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class InputDevice implements Device {
    private final String name;
    private final List<Setting> setting;
    private final List<Condition> condition;

    public InputDevice(String name, List<Setting> setting, List<Condition> condition) {
        this.name = name;
        this.setting = Collections.unmodifiableList(setting);
        this.condition = Collections.unmodifiableList(condition);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Setting> getSetting() {
        return setting;
    }

    public List<Condition> getCondition() {
        return condition;
    }
}

/*public enum InputDevice {
    TEMPERATURE("Temperature", Arrays.asList()),
    LIGHT("Light", Arrays.asList());

    private final String name;
    private final List<DiagramEdge> condition;

    InputDevice(String name, List<DiagramEdge> condition) {
        this.name = name;
        this.condition = condition;
    }

    public String getName() {
        return name;
    }

    public List<DiagramEdge> getCondition() {
        return condition;
    }
}*/
