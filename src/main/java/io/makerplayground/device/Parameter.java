package io.makerplayground.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
@JsonDeserialize(using = ParameterDeserializer.class)
public class Parameter {
    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final DataType dataType;
    private final ControlType controlType;

    public Parameter(String name, Object defaultValue, Constraint constraint, DataType dataType, ControlType controlType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.dataType = dataType;
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

    public DataType getDataType() {
        return dataType;
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
                ", dataType=" + dataType +
                ", controlType=" + controlType +
                '}';
    }
}
