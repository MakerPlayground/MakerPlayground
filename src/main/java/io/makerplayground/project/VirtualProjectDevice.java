package io.makerplayground.project;

import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

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
        public static final GenericDevice timeGenericDevice = new GenericDevice("Time Elapsed", "", GenericDeviceType.UTILITY,
                Collections.emptyList(), List.of(lessThan, greaterThan), Collections.emptyList());
        public static final VirtualProjectDevice projectDevice = new VirtualProjectDevice("Time Elapsed", timeGenericDevice, false);
    }

    public static class Memory {
        // Memory
        static final ObservableList<ProjectValue> variables = FXCollections.observableArrayList();
        public static final ObservableList<ProjectValue> unmodifiableVariables = FXCollections.unmodifiableObservableList(variables);
        public static final Condition compare = new Condition("Compare", "", Collections.emptyList());
        public static final Parameter nameParameter = new Parameter("name", DataType.VARIABLE_NAME, "x", Constraint.NONE, ControlType.VARIABLE);
        public static final Parameter valueParameter = new Parameter("Value", DataType.DOUBLE, new NumberWithUnit(0.0, Unit.NOT_SPECIFIED), Constraint.createNumericConstraint(-Double.MAX_VALUE, Double.MAX_VALUE, Unit.NOT_SPECIFIED), ControlType.SPINBOX);
        public static final Action setValue = new Action("Set Value", "setValue", List.of(nameParameter, valueParameter));
        public static final GenericDevice memoryGenericDevice = new GenericDevice("Memory", "", GenericDeviceType.UTILITY, List.of(setValue), List.of(compare), new ArrayList<>());
        public static final VirtualProjectDevice projectDevice = new VirtualProjectDevice("Memory", memoryGenericDevice, true);
    }

    private final boolean allowRepeat;

    public static class All {
        // other virtual device such as the statistic calculator etc. should be defined here
        public static final List<ProjectDevice> virtualDevices = List.of(TimeElapsed.projectDevice, Memory.projectDevice);
        public static final List<ProjectDevice> virtualDevicesHaveCondition = virtualDevices.stream().filter(projectDevice -> !projectDevice.getGenericDevice().getCondition().isEmpty()).collect(Collectors.toUnmodifiableList());
        public static final List<ProjectDevice> virtualDevicesHaveAction = virtualDevices.stream().filter(projectDevice -> !projectDevice.getGenericDevice().getAction().isEmpty()).collect(Collectors.toUnmodifiableList());
    }

    public VirtualProjectDevice(String name, GenericDevice genericDevice, boolean allowRepeat) {
        super(name, genericDevice);
        this.allowRepeat = allowRepeat;
    }

    public boolean isAllowRepeat() {
        return allowRepeat;
    }
}
