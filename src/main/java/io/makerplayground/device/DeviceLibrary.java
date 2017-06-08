package io.makerplayground.device;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;

import java.util.*;

/**
 *
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public enum DeviceLibrary {
    INSTANCE;

    private final Map<String, OutputDevice> outputDevice;
    private final Map<String, InputDevice> inputDevice;

    DeviceLibrary() {
        outputDevice = new HashMap<>();
        inputDevice = new HashMap<>();

        // TODO: Replace these dummy devices by loading output device and inputDevice from file or the server
        OutputDevice led = new OutputDevice("led", Collections.EMPTY_LIST, Arrays.asList(
                new Action("on", Arrays.asList(new Parameter("brightness", ParameterType.NUMERIC_SLIDER))),
                new Action("off", Collections.EMPTY_LIST)
        ));
        outputDevice.put("led", led);

        OutputDevice speaker = new OutputDevice("speaker", Collections.EMPTY_LIST, Arrays.asList(
                new Action("play", Arrays.asList(new Parameter("volume", ParameterType.NUMERIC_TEXTBOX)))
        ));
        outputDevice.put("speaker", speaker);
    }

    /**
     * Get an object represent the output device by name.
     * This method is mainly when read the project from the file.
     * @param name
     * @return an output device
     */
    public OutputDevice getOutputDevice(String name) {
        return outputDevice.get(name);
    }

    public Collection<OutputDevice> getOutputDevice() {
        return Collections.unmodifiableCollection(outputDevice.values());
    }

    public InputDevice getInputDevice(String name) {
        return inputDevice.get(name);
    }

    public Collection<InputDevice> getInputDevice() {
        return Collections.unmodifiableCollection(inputDevice.values());
    }
}
