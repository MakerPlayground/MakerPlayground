package io.makerplayground.device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nuntipat Narkthong on 6/5/2017 AD.
 */
public enum DeviceLibrary {
    INSTANCE;

    private final Map<GenericDevice, List<Device>> inputDevice;
    private final Map<GenericDevice, List<Device>> outputDevice;

    DeviceLibrary() {
        Map<GenericDevice, List<Device>> tmpInputDevice = new HashMap<>();
        Map<GenericDevice, List<Device>> tmpOutputDevice = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        List<GenericDevice> temp = null;

        try {
//            mapper.writeValue(new File("device.json"), tmpOutputDevice.keySet());
            temp = mapper.readValue(getClass().getResourceAsStream("/json/genericoutputdevice.json")
                    , new TypeReference<List<GenericDevice>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (GenericDevice device : temp) {
            tmpOutputDevice.put(device, Collections.emptyList());
        }

        //System.out.println(temp);

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
