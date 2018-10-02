package io.makerplayground.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;

@JsonDeserialize(using = PropertyDeserializer.class)
public class Property {

    private final String name;
    private final Object defaultValue;
    private final Constraint constraint;
    private final DataType dataType;
    private final ControlType controlType;

    /**
     * The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param name name of this parameter ex. brightness
     * @param defaultValue the default value of this parameter ex. 100
     * @param constraint the constraint of this parameter as an instance of {@link Constraint}
     * @param dataType an enumerated value ({@link DataType}) indicating type of this parameter
     * @param controlType an enumerated value ({@link ControlType}) indicating type of a UI control to be used
     */
    Property(String name, Object defaultValue, Constraint constraint, DataType dataType, ControlType controlType) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.dataType = dataType;
        this.controlType = controlType;
    }

    /**
     * Get the name of this parameter
     * @return name of this parameter ex. brightness, speed, etc.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the default value of this parameter
     * @return the default value of this parameter
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the constraint of this parameter
     * @return the constrint of this parameter
     */
    public Constraint getConstraint() {
        return constraint;
    }

    public double getMinimumValue() {
        return ((NumericConstraint) constraint).getMin();
    }

    public double getMaximumValue() {
        return ((NumericConstraint) constraint).getMax();
    }

    /**
     * Get the datatype of this parameter as an instance of {@link DataType}
     * @return the datatype of this parameter
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Get the suggest control type of this parameter as an instance of {@link ControlType}
     * @return the type of control to be used to adjust this parameter in the GUI
     */
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
