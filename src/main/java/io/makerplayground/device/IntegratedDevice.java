package io.makerplayground.device;

import io.makerplayground.helper.DeviceType;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IntegratedDevice extends Device {
    public IntegratedDevice(String model, String mpLibrary, List<String> externalLibrary, List<DevicePort> port, List<Peripheral> connectivity
            , Map<GenericDevice, Integer> supportedDevice, Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction
            , Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition, Map<GenericDevice, Map<Value, Constraint>> supportedValue) {
        super("", "", model, "", 0, 0, DeviceType.INTEGRATED, "", FormFactor.NONE
                , mpLibrary, externalLibrary, Collections.emptySet(), null, port, connectivity, supportedDevice
                , supportedAction, supportedCondition, supportedValue, null, null, Collections.emptyList()
                , Collections.emptyMap(), Collections.emptyList());
    }
}
