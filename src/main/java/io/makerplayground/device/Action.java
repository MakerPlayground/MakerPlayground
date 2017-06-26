package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * Class to represent an action can be performed by a device.
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class Action {
    private final String name;
    private final ActionType type;
    private final List<Parameter> parameter;

    /**
     * Create a new action. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param name name of this action
     * @param type type of this action
     * @param parameter list of parameter
     */
    @JsonCreator
    Action(@JsonProperty("name") String name,@JsonProperty("type") ActionType type,@JsonProperty("parameter") List<Parameter> parameter) {
        this.name = name;
        this.type = type;
        this.parameter = Collections.unmodifiableList(parameter);
    }

    /**
     * Get the name of this action
     * @return name of this action ex. on, off, blink
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of this action
     * @return type of this action which is ActionType.active or ActionType.Inactive
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Get the list of parameter of this action
     * @return unmodifiable list of parameter of this action
     */
    public List<Parameter> getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", parameter=" + parameter +
                '}';
    }
}
