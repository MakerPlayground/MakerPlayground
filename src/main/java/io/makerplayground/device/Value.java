package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
public class Value {
    private final String name;
    private final DataType type;
    private final Constraint constraint;

    @JsonCreator
    public Value(@JsonProperty("name") String name, @JsonProperty("datatype") DataType type, @JsonProperty("constraint") Constraint constraint) {
        this.name = name;
        this.type = type;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public Constraint getConstraint() {
        return constraint;
    }

}
