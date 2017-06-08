package io.makerplayground.device;

import java.util.Collections;
import java.util.List;

/**
 * Created by nuntipat on 6/5/2017 AD.
 */
public class Action {
    private final String name;
    private final List<Parameter> parameter;

    public Action(String name, List<Parameter> parameter) {
        this.name = name;
        this.parameter = Collections.unmodifiableList(parameter);
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameter() {
        return parameter;
    }
}
