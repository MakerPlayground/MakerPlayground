package io.makerplayground.ui;

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.dialog.generate.DiagramPane;
import io.makerplayground.ui.explorer.DeviceExplorerPanel;
import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceTab extends SplitPane {

    private final Project currentProject;

    private final DeviceExplorerPanel deviceExplorerPanel;
    private final Tab deviceConfigTab;

    private final DoubleProperty deviceDiagramZoomLevel = new SimpleDoubleProperty(DiagramPane.DEFAULT_ZOOM_SCALE);

    public DeviceTab(Project currentProject, HostServices hostServices) {
        this.currentProject = currentProject;

        // device explorer
        deviceExplorerPanel = new DeviceExplorerPanel(currentProject.getSelectedController(), hostServices);
        deviceExplorerPanel.setOnAddButtonPressed(actualDevice -> {
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

        // perform layout
        deviceConfigTab = new Tab("Device Configuration");
        deviceConfigTab.setClosable(false);
        Tab deviceExplorerTab = new Tab("Device Explorer", deviceExplorerPanel);
        deviceExplorerTab.setClosable(false);
        TabPane deviceTabPane = new TabPane(deviceConfigTab, deviceExplorerTab);
        deviceTabPane.setMaxWidth(710);

        setDividerPositions(0.5);
        setOrientation(Orientation.HORIZONTAL);
        getItems().addAll(deviceTabPane, new Pane());
        getStylesheets().add(getClass().getResource("/css/DeviceTab.css").toExternalForm());

        refreshConfigDevicePane();
        refreshDiagramAndExplorer();
    }

    void refreshConfigDevicePane() {
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(currentProject);
        configActualDeviceViewModel.setConfigChangedCallback(this::refreshDiagramAndExplorer);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
        deviceConfigTab.setContent(configActualDeviceView);
    }

    private void refreshDiagramAndExplorer() {
        deviceExplorerPanel.setController(currentProject.getSelectedController());

        DiagramPane diagramPane = new DiagramPane(currentProject);
        diagramPane.setZoomLevel(deviceDiagramZoomLevel.get());
        getItems().set(1, diagramPane);

        deviceDiagramZoomLevel.unbind();
        deviceDiagramZoomLevel.bind(diagramPane.zoomLevelProperty());
    }

}
