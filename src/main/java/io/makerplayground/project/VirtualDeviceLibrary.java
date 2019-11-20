package io.makerplayground.project;

import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.Constraint;

import java.util.Collections;
import java.util.List;

public class VirtualDeviceLibrary {

    public static class TimeElapse {
        public static final Value VALUE = new Value("value", DataType.DOUBLE, Constraint.createNumericConstraint(0, Double.MAX_VALUE, Unit.SECOND));
        public static final Condition FROM_LAST_BLOCK_CONDITION = new Condition("Time from last block", "", Collections.emptyList());
        public static final GenericDevice TIME_ELAPSED_GENERIC_DEVICE = new GenericDevice("Time Elapsed", "", GenericDeviceType.UTILITY, Collections.emptyList(), List.of(FROM_LAST_BLOCK_CONDITION), List.of(VALUE));
        public static final ProjectDevice PROJECT_DEVICE = new ProjectDevice("Time Elapsed", TIME_ELAPSED_GENERIC_DEVICE);
    }

    // other virtual device such as the statistic calculator etc. should be defined here

    public static final List<ProjectDevice> DEVICES = List.of(TimeElapse.PROJECT_DEVICE);
}
