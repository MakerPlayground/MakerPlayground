package io.makerplayground.device;

import java.util.*;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public enum DeviceLibrary {
    INSTANCE;

    private final Map<GenericDevice, List<Device>> inputDevice;
    private final Map<GenericDevice, List<Device>> outputDevice;

    DeviceLibrary() {
        Map<GenericDevice, List<Device>>  tmpInputDevice = new HashMap<>();
        Map<GenericDevice, List<Device>>  tmpOutputDevice = new HashMap<>();

        // TODO: Replace these dummy devices by loading output device and inputDevice from file or the server
        GenericDevice led = new GenericDevice("led", Arrays.asList(
                new Action("on", ActionType.Active, Arrays.asList(new Parameter("brightness", Constraint.ZERO_TO_HUNDRED, ParameterType.DOUBLE, ControlType.NUMERIC_SLIDER))),
                new Action("off", ActionType.Inactive, Collections.emptyList())
        ), Collections.emptyList());
        tmpOutputDevice.put(led, Collections.emptyList());

        GenericDevice speaker = new GenericDevice("speaker", Arrays.asList(
                new Action("play", ActionType.Active, Arrays.asList(new Parameter("volume", Constraint.ZERO_TO_HUNDRED, ParameterType.DOUBLE, ControlType.NUMERIC_TEXTBOX)))
        ), Collections.emptyList());
        tmpOutputDevice.put(speaker, Collections.emptyList());

        this.inputDevice = Collections.unmodifiableMap(tmpInputDevice);
        this.outputDevice = Collections.unmodifiableMap(tmpOutputDevice);
    }

    public Set<GenericDevice> getInputDevice() {
        return inputDevice.keySet();
    }

    public Set<GenericDevice> getOutputDevice() {
        return outputDevice.keySet();
    }
}
