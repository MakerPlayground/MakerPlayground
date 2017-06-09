package io.makerplayground.device;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public class Parameter {
    private String name;
    private ParameterType parameterType;
    private Class<?> type;

    public Parameter(String name, ParameterType parameterType, Class<?> type) {
        this.name = name;
        this.parameterType = parameterType;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public Class<?> getType() {
        return type;
    }
}
