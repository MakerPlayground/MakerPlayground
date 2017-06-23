package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class GenericDevice {
    private final String name;
    private final List<Action> action;
    private final Action defaultAction;
    private final List<Value> value;

    @JsonCreator
    public GenericDevice(@JsonProperty("name") String name,@JsonProperty("action") List<Action> action,@JsonProperty("defaultAction") Action defaultAction,@JsonProperty("value") List<Value> value) {
        this.name = name;
        this.action = Collections.unmodifiableList(action);
        this.defaultAction = defaultAction;
        this.value = Collections.unmodifiableList(value);
    }

    public String getName() {
        return name;
    }

    public List<Action> getAction() {
        return action;
    }

    public Action getDefaultAction() {
        return defaultAction;
    }

    public List<Value> getValue() {
        return value;
    }
}
