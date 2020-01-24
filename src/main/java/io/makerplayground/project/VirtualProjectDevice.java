package io.makerplayground.project;

import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.constraint.NumericConstraint;

import java.util.Collections;
import java.util.List;

public class VirtualProjectDevice {
    // time elapsed
    public static final io.makerplayground.device.shared.Condition lessThan = new io.makerplayground.device.shared.Condition("Less than", "", List.of(new Parameter("value", DataType.DOUBLE,
            new NumberWithUnit(0, Unit.MILLISECOND), new NumericConstraint(0, Double.MAX_VALUE, Unit.MILLISECOND), ControlType.SPINBOX)));
    public static final io.makerplayground.device.shared.Condition greaterThan = new io.makerplayground.device.shared.Condition("More than", "", List.of(new Parameter("value", DataType.DOUBLE,
            new NumberWithUnit(0, Unit.MILLISECOND), new NumericConstraint(0, Double.MAX_VALUE, Unit.MILLISECOND), ControlType.SPINBOX)));
    public static final GenericDevice timeGenericDevice = new GenericDevice("Time Elapsed", "", GenericDeviceType.UTILITY,
            Collections.emptyList(), List.of(lessThan, greaterThan), Collections.emptyList());
    public static final ProjectDevice timeElapsedProjectDevice = new ProjectDevice("Time Elapsed", timeGenericDevice);

    // other virtual device such as the statistic calculator etc. should be defined here

    public static final List<ProjectDevice> virtualDevices = List.of(timeElapsedProjectDevice);
}
