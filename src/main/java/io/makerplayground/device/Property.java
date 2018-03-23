package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.makerplayground.helper.DataType;

public class Property {
    private final String name;
    private final DataType type;

    @JsonCreator
    public Property(@JsonProperty("name") String name, @JsonProperty("type") DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }
}
