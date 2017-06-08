package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.InputDevice;
import io.makerplayground.device.OutputDevice;
import io.makerplayground.project.Project;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorViewModel {
    private final Project project;
    private final ObservableMap<InputDevice, SimpleIntegerProperty> inputDeviceMap;
    private final ObservableMap<OutputDevice, SimpleIntegerProperty> outputDeviceMap;

    public DeviceSelectorViewModel(Project project) {
        this.project = project;

        this.inputDeviceMap = FXCollections.observableHashMap();
        for (InputDevice device : DeviceLibrary.INSTANCE.getInputDevice()) {
            inputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }

        this.outputDeviceMap = FXCollections.observableHashMap();
        for (OutputDevice device : DeviceLibrary.INSTANCE.getOutputDevice()) {
            outputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }
    }

    public ObservableMap<InputDevice, SimpleIntegerProperty> getInputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(inputDeviceMap);
    }

    public ObservableMap<OutputDevice, SimpleIntegerProperty> getOutputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(outputDeviceMap);
    }

    public void increaseDeviceCount(InputDevice device) {
        //inputDeviceMap.get(device, inputDeviceMap.get(device). + 1);
    }

    public void increaseDeviceCount(OutputDevice device) {
        //outputDeviceMap.put(device, outputDeviceMap.get(device) + 1);
    }

    public void decreaseDeviceCount(InputDevice device) {
        //inputDeviceMap.put(device, inputDeviceMap.get(device) - 1);
    }

    public void decreaseDeviceCount(OutputDevice device) {
        //outputDeviceMap.put(device, outputDeviceMap.get(device) - 1);
    }

    public void importDeviceToProject() {
//        for (Map.Entry<InputDevice, Integer> entry : inputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addInputDevice(entry.getKey());
//            }
//        }
//
//        for (Map.Entry<OutputDevice, Integer> entry : outputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addOutputDevice(entry.getKey());
//            }
//        }

        // TODO: clear the map value to 0
    }
}
