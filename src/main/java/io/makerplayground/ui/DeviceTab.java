package io.makerplayground.ui;

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.devicetab.*;
import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceTab extends SplitPane {

    private final Project currentProject;

    private final DeviceExplorerView deviceExplorerView;
    private final DeviceVersionPane versionPane;
    private final Tab circuitDiagramTab;
    private final TabPane deviceTabPane;

    private final DoubleProperty deviceDiagramZoomLevel = new SimpleDoubleProperty(DeviceDiagramView.DEFAULT_ZOOM_SCALE);
    private final DoubleProperty deviceDiagramHScrollPosition = new SimpleDoubleProperty(0);
    private final DoubleProperty deviceDiagramVScrollPosition = new SimpleDoubleProperty(0);

    public DeviceTab(Project currentProject, HostServices hostServices) {
        this.currentProject = currentProject;

        // device explorer
        deviceExplorerView = new DeviceExplorerView(currentProject, hostServices);
        deviceExplorerView.setOnAddButtonPressed(actualDevice -> {
            List<ProjectDevice> projectDevices = new ArrayList<>();
            for (GenericDevice genericDevice : actualDevice.getSupportedGenericDevice()) {
                projectDevices.add(currentProject.addDevice(genericDevice));
            }

            // sort by name so that the top most device will be the parent device for the other project device
            projectDevices.sort(Comparator.comparing(ProjectDevice::getName));

            ProjectDevice parentDevice = projectDevices.get(0);
            currentProject.getProjectConfiguration().setActualDevice(parentDevice, actualDevice);
            for (int i=1; i<projectDevices.size(); i++) {
                currentProject.getProjectConfiguration().setIdenticalDevice(projectDevices.get(i), parentDevice);
            }

            refreshConfigDevicePane();
            refreshDiagramAndExplorer();
        });
        VBox.setVgrow(deviceExplorerView, Priority.ALWAYS);

        versionPane = new DeviceVersionPane();

        VBox vbox = new VBox();
        vbox.getChildren().addAll(deviceExplorerView, versionPane);

        // perform layout
        Tab deviceExplorerTab = new Tab("Device Explorer", vbox);
        deviceExplorerTab.setClosable(false);
        circuitDiagramTab = new Tab("Circuit Diagram");
        circuitDiagramTab.setClosable(false);
        deviceTabPane = new TabPane(deviceExplorerTab, circuitDiagramTab);
        deviceTabPane.setMinWidth(480);

        setDividerPositions(0.5);
        setOrientation(Orientation.HORIZONTAL);
        getItems().addAll(new Pane(), deviceTabPane);
        getStylesheets().add(getClass().getResource("/css/DeviceTab.css").toExternalForm());

        refreshConfigDevicePane();
        refreshDiagramAndExplorer();
    }

    void refreshConfigDevicePane() {
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(currentProject);
        configActualDeviceViewModel.setConfigChangedCallback(this::refreshDiagramAndExplorer);
        ActualDeviceConfigView actualDeviceConfigView = new ActualDeviceConfigView(configActualDeviceViewModel);
        getItems().setAll(actualDeviceConfigView, deviceTabPane);
    }

    private void refreshDiagramAndExplorer() {
        deviceExplorerView.setController(currentProject.getSelectedController());

        DeviceDiagramView deviceDiagramView = new DeviceDiagramView(currentProject);
        deviceDiagramView.setZoomLevel(deviceDiagramZoomLevel.get());
        deviceDiagramView.setHScrollPosition(deviceDiagramHScrollPosition.get());
        deviceDiagramView.setVScrollPosition(deviceDiagramVScrollPosition.get());
        circuitDiagramTab.setContent(deviceDiagramView);

        deviceDiagramZoomLevel.unbind();
        deviceDiagramZoomLevel.bind(deviceDiagramView.zoomLevelProperty());
        deviceDiagramHScrollPosition.unbind();
        deviceDiagramHScrollPosition.bind(deviceDiagramView.hScrollPositionProperty());
        deviceDiagramVScrollPosition.unbind();
        deviceDiagramVScrollPosition.bind(deviceDiagramView.vScrollPositionProperty());
    }

    public void setOnLibraryUpdateButtonPressed(EventHandler<ActionEvent> eventHandler) {
        versionPane.setOnLibraryUpdateButtonPressed(eventHandler);
    }
}
