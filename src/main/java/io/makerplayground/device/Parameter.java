package io.makerplayground.device;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class Parameter {
    private final String name;
    private final Constraint constraint;
    private final ParameterType parameterType;
    private final ControlType controlType;

    public Parameter(String name, Constraint constraint, ParameterType parameterType, ControlType controlType) {
        this.name = name;
        this.constraint = constraint;
        this.parameterType = parameterType;
        this.controlType = controlType;
    }

    public String getName() {
        return name;
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
