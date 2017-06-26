package io.makerplayground.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
@JsonSerialize(using = ParameterSerializer.class)
@JsonDeserialize(using = ParameterDeserializer.class)
public class Parameter {
    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final ParameterType parameterType;
    private final ControlType controlType;

    public Parameter(String name, Object defaultValue, Constraint constraint, ParameterType parameterType, ControlType controlType) {
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

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", defaultValue=" + defaultValue +
                ", constraint=" + constraint +
                ", parameterType=" + parameterType +
                ", controlType=" + controlType +
                '}';
    }
}
