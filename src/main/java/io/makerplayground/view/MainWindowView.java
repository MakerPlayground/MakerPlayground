package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.makerplayground.viewmodel.MainWindowViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/MainWindowView.fxml")
public class MainWindowView implements FxmlView<MainWindowViewModel>, Initializable {

    @InjectViewModel
    private MainWindowViewModel viewModel;

    @FXML
    private StackPane root;

    @FXML
    private AnchorPane diagramTab;

    @FXML
    private SplitPane deviceConfigTab;

    @FXML
    private SplitPane deviceMonitorTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        diagramTab.visibleProperty().bind(viewModel.diagramEditorShowingProperty());
        diagramTab.managedProperty().bind(diagramTab.visibleProperty());

        deviceConfigTab.visibleProperty().bind(viewModel.deviceConfigShowingProperty());
        deviceConfigTab.managedProperty().bind(deviceConfigTab.visibleProperty());

        deviceMonitorTab.visibleProperty().bind(viewModel.deviceMonitorShowingProperty());
        deviceMonitorTab.managedProperty().bind(deviceMonitorTab.visibleProperty());

//        vm.deviceMonitorShowingProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue) {
//                deviceMonitor.closePort();
//            } else {
//                // TODO: handle the case when initialize failed by switch to the old tab
//                deviceMonitor.initialize(serialPort.get());
//                root.setCenter(deviceMonitor);
//            }
//        });

//        project.addListener((observable, oldValue, newValue) -> {
//            currentProject = newValue;
//
//            diagramEditor = initDiagramEditor();
//            deviceTab = new DeviceTab(project.get(), hostServices);
//            deviceMonitor.closePort();
//            deviceMonitor = new DeviceMonitor();
//
//            if (vm.isDiagramEditorShowing()) {
//                setCenter(diagramEditor);
//            } else if (vm.isDeviceConfigShowing()) {
//                setCenter(deviceTab);
//            } else {
//                setCenter(deviceMonitor);
//            }
//        });
    }

//    private final HostServices hostServices;

//    private Project currentProject;
//    private ReadOnlyObjectProperty<SerialPort> serialPort;

//    private Node diagramEditor;
//    private DeviceTab deviceTab;
//    private DeviceMonitor deviceMonitor;


//    public MainWindowView(ObjectProperty<Project> project, ReadOnlyObjectProperty<SerialPort> serialPort, HostServices hostServices) {
//        this.currentProject = project.get();
//        this.serialPort = serialPort;
//        this.hostServices = hostServices;
//    }

//    private Node initDiagramEditor() {
//        CanvasViewModel canvasViewModel = new CanvasViewModel(currentProject);
//        return new CanvasView(canvasViewModel);
//    }
}
