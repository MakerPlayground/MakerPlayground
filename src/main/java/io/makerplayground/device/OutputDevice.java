package io.makerplayground.device;

import java.util.Collections;
import java.util.List;

/**
 * Class to
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class OutputDevice implements Device {
    private final String name;
    private final List<Setting> setting;
    private final List<Action> action;

    public OutputDevice(String name, List<Setting> setting, List<Action> action) {
        this.name = name;
        this.setting = Collections.unmodifiableList(setting);
        this.action = Collections.unmodifiableList(action);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Setting> getSetting() {
        return setting;
    }

    public List<Action> getAction() {
        return action;
    }
}
