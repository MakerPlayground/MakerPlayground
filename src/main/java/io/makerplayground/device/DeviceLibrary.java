package io.makerplayground.device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
        ObjectMapper mapper = new ObjectMapper();

        // TODO: Replace these dummy devices by loading output device and inputDevice from file or the server
        Action action1 = new Action("on", ActionType.Active, Arrays.asList(new Parameter("brightness", 100, Constraint.ZERO_TO_HUNDRED, ParameterType.DOUBLE, ControlType.NUMERIC_SLIDER)));
        Action action2 = new Action("off", ActionType.Inactive, Collections.emptyList());
        GenericDevice led = new GenericDevice("led", Arrays.asList(action1, action2), action2, Collections.emptyList());
        tmpOutputDevice.put(led, Collections.emptyList());

        Action action3 = new Action("play", ActionType.Active, Arrays.asList(new Parameter("volume", 50, Constraint.ZERO_TO_HUNDRED, ParameterType.DOUBLE, ControlType.NUMERIC_TEXTBOX)));
        Action action4 = new Action("stop", ActionType.Inactive, Collections.emptyList());
        GenericDevice speaker = new GenericDevice("speaker", Arrays.asList(action3, action4), action4, Collections.emptyList());
        tmpOutputDevice.put(speaker, Collections.emptyList());

        List <GenericDevice> temp = null;
        try {
//            Map<GenericDevice, List<Device>> temp = mapper.readValue(new File("device.json"), Map.class);
//            mapper.writeValue(new File("device.json"), tmpOutputDevice.keySet());
            temp = mapper.readValue(new File("device.json"), new TypeReference<List<GenericDevice>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0 ; i<temp.size() ; i++){
            tmpOutputDevice.put(temp.get(i), Collections.emptyList());
        }

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
