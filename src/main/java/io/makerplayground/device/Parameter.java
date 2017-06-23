package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class Parameter {
    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final ParameterType parameterType;
    private final ControlType controlType;

    @JsonCreator
    public Parameter(@JsonProperty("name") String name,@JsonProperty("defaultValue") Object defaultValue,@JsonProperty("constraint") Constraint constraint,@JsonProperty("parameterType") ParameterType parameterType,@JsonProperty("controlType") ControlType controlType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.parameterType = parameterType;
        this.controlType = controlType;
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public ControlType getControlType() {
        return controlType;
    }
}
