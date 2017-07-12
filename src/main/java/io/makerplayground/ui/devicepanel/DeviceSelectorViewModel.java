package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorViewModel {
    private final Project project;
    private final ObservableMap<GenericDevice, SimpleIntegerProperty> inputDeviceMap;
    private final ObservableMap<GenericDevice, SimpleIntegerProperty> outputDeviceMap;

    public DeviceSelectorViewModel(Project project) {
        this.project = project;

        this.inputDeviceMap = FXCollections.observableHashMap();
        for (GenericDevice device : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
            inputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }

        this.outputDeviceMap = FXCollections.observableHashMap();
        for (GenericDevice device : DeviceLibrary.INSTANCE.getGenericOutputDevice()) {
            outputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }
    }

    public ObservableMap<GenericDevice, SimpleIntegerProperty> getInputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(inputDeviceMap);
    }

    public ObservableMap<GenericDevice, SimpleIntegerProperty> getOutputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(outputDeviceMap);
    }


    public void importDeviceToProject() {
//        for (Map.Entry<GenericDevice, Integer> entry : inputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addInputDevice(entry.getKey());
//            }
//        }
//
//        for (Map.Entry<GenericOutputDevice, Integer> entry : outputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addOutputDevice(entry.getKey());
//            }
//        }

        // TODO: clear the map value to 0
    }
}
