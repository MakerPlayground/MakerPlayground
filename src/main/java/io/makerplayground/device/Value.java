package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
public class Value {
    private final String name;
    private final Constraint constraint;

    @JsonCreator
    public Value(@JsonProperty("name") String name,@JsonProperty("constraint") Constraint constraint) {
        this.name = name;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public Constraint getConstraint() {
        return constraint;
    }

}
