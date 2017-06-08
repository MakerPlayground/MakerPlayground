package io.makerplayground.device;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class Parameter {
    private String name;
    private ParameterType parameterType;

    public Parameter(String name, ParameterType parameterType) {
        this.name = name;
        this.parameterType = parameterType;
    }

    public String getName() {
        return name;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }
}
