package io.makerplayground.project;

import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VirtualProjectDevice extends ProjectDevice {
    // time elapsed
    public static class TimeElapsed {
        public static final Condition lessThan = new Condition("Less than", "", List.of(new Parameter("value", DataType.DOUBLE,
                new NumberWithUnit(0, Unit.MILLISECOND), new NumericConstraint(0, 1000, Unit.MILLISECOND), ControlType.SPINBOX)));
        public static final Condition greaterThan = new Condition("More than", "", List.of(new Parameter("value", DataType.DOUBLE,
                new NumberWithUnit(0, Unit.MILLISECOND), new NumericConstraint(0, 1000, Unit.MILLISECOND), ControlType.SPINBOX)));
        // TODO: should we use type LONG or ULONG instead? (this may require us to update the value system to support other numerical types)
        public static final Value timeSincePowerOn = new Value("Time since power on", DataType.DOUBLE, new NumericConstraint(0, Double.MAX_VALUE, Unit.MILLISECOND));
        public static final Value timeSinceLastBlock = new Value("Time since last block", DataType.DOUBLE, new NumericConstraint(0, Double.MAX_VALUE, Unit.MILLISECOND));
        public static final GenericDevice timeGenericDevice = new GenericDevice("Time Elapsed", "", GenericDeviceType.UTILITY,
                Collections.emptyList(), List.of(lessThan, greaterThan), List.of(timeSincePowerOn, timeSinceLastBlock));
        public static final VirtualProjectDevice projectDevice = new VirtualProjectDevice("Time Elapsed", timeGenericDevice, false);
    }

    // Memory
    public static class Memory {
        public static final Condition compare = new Condition("Compare", "", Collections.emptyList());
        public static final Parameter nameParameter = new Parameter("Name", DataType.VARIABLE_NAME, "x", Constraint.NONE, ControlType.VARIABLE);
        public static final Parameter valueParameter = new Parameter("Value", DataType.DOUBLE, new NumberWithUnit(0.0, Unit.NOT_SPECIFIED), Constraint.createNumericConstraint(-Double.MAX_VALUE, Double.MAX_VALUE, Unit.NOT_SPECIFIED), ControlType.SPINBOX);
        public static final Action setValue = new Action("Set Value", "setValue", List.of(nameParameter, valueParameter));
        public static final GenericDevice memoryGenericDevice = new GenericDevice("Memory", "", GenericDeviceType.UTILITY, List.of(setValue), List.of(compare), new ArrayList<>());
        public static final VirtualProjectDevice projectDevice = new VirtualProjectDevice("Memory", memoryGenericDevice, true);
    }

    // other virtual device such as the statistic calculator etc. should be defined here

    public static List<ProjectDevice> getDevices() {
        return List.of(TimeElapsed.projectDevice, Memory.projectDevice);
    }

    public static List<ProjectDevice> getDevicesWithAction() {
        return getDevices().stream().filter(projectDevice -> projectDevice.getGenericDevice().hasAction()).collect(Collectors.toUnmodifiableList());
    }

    public static List<ProjectDevice> getDevicesWithCondition() {
        return getDevices().stream().filter(projectDevice -> projectDevice.getGenericDevice().hasCondition()).collect(Collectors.toUnmodifiableList());
    }

    private final boolean allowRepeat;

    public VirtualProjectDevice(String name, GenericDevice genericDevice, boolean allowRepeat) {
        super(name, genericDevice);
        this.allowRepeat = allowRepeat;
    }

    public boolean isAllowRepeat() {
        return allowRepeat;
    }
}
