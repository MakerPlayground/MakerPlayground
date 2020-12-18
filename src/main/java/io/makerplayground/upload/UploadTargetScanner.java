package io.makerplayground.upload;

import io.makerplayground.project.Project;
import io.makerplayground.util.RpiDiscoverer;
import io.makerplayground.util.RpiKeepAliveChecker;
import io.makerplayground.util.SerialPortDiscoverer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class UploadTargetScanner {

    private final ObjectProperty<Project> project;

    private final ObservableList<UploadTarget> serialPortList = FXCollections.observableArrayList();
    private final ObservableList<UploadTarget> rpiHostList = FXCollections.observableArrayList();
    private final ObjectProperty<ObservableList<UploadTarget>> uploadTargetList = new SimpleObjectProperty<>();

    RpiDiscoverer rpiDiscoverer = new RpiDiscoverer(rpiHostList);
    RpiKeepAliveChecker rpiKeepAliveChecker = new RpiKeepAliveChecker(rpiHostList);
    SerialPortDiscoverer serialPortDiscoverer = new SerialPortDiscoverer(serialPortList);

    public UploadTargetScanner(ObjectProperty<Project> project) {
        this.project = project;
    }

    public ObjectProperty<ObservableList<UploadTarget>> uploadTargetListProperty() {
        return uploadTargetList;
    }

    public void scan() {
        List<UploadMode> supportMethods = project.get().getSelectedPlatform().getSupportUploadModes();
        if (supportMethods.contains(UploadMode.SERIAL_PORT)) {
            uploadTargetList.set(serialPortList);
            if (!serialPortDiscoverer.isRunning()) {
                serialPortDiscoverer.startScan();
            }
            if (rpiDiscoverer.isRunning()) {
                rpiDiscoverer.stopScan();
            }
            if (rpiKeepAliveChecker.isRunning()) {
                rpiKeepAliveChecker.stopScan();
            }
        }
        if (supportMethods.contains(UploadMode.RPI_ON_NETWORK)) {
            uploadTargetList.set(rpiHostList);
            if (serialPortDiscoverer.isRunning()) {
                serialPortDiscoverer.stopScan();
            }
            if (!rpiDiscoverer.isRunning()) {
                rpiDiscoverer.startScan();
            }
            if (!rpiKeepAliveChecker.isRunning()) {
                rpiKeepAliveChecker.startScan();
            }
        }
    }
}
