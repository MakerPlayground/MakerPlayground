package io.makerplayground.ui.explorer;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.diagram.InteractiveWiringDiagram;
import io.makerplayground.project.Project;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class InteractiveDiagramView extends HBox {

    @FXML private ScrollPane diagramScrollPane;
    @FXML private ComboBox<SerialPort> portCombobox;
    @FXML private Button startInteractiveModeButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomDefaultButton;

    private static final double DEFAULT_ZOOM_SCALE = 0.5;
    private DoubleProperty scale = new SimpleDoubleProperty(DEFAULT_ZOOM_SCALE);

    public InteractiveDiagramView(Project project, InteractiveModel interactiveModel) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/generate/WiringDiagramView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        portCombobox.getItems().setAll(SerialPort.getCommPorts());
        portCombobox.setOnShowing(event -> portCombobox.getItems().setAll(SerialPort.getCommPorts()));
        portCombobox.disableProperty().bind(interactiveModel.initializeProperty());

        startInteractiveModeButton.disableProperty().bind(portCombobox.getSelectionModel().selectedItemProperty().isNull()
                .and(interactiveModel.initializeProperty().not()));

        zoomInButton.setOnAction(event -> scale.set(scale.get() + 0.1));
        zoomOutButton.setOnAction(event -> scale.set(Math.max(0.1, scale.get() - 0.1)));
        zoomDefaultButton.setOnAction(event -> scale.set(0.5));

        interactiveModel.initializeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                startInteractiveModeButton.setText("Stop Interactive Mode");
            } else {
                startInteractiveModeButton.setText("Start Interactive Mode");
            }
        });

        if (DeviceMapper.validateDeviceAssignment(project) == DeviceMapperResult.OK) {
            Pane wiringDiagram = InteractiveWiringDiagram.make(project, interactiveModel);
            wiringDiagram.scaleXProperty().bind(scale);
            wiringDiagram.scaleYProperty().bind(scale);
            diagramScrollPane.setContent(new Group(wiringDiagram));
        }
    }

    public void setOnInteractiveControlButtonPressed(EventHandler<ActionEvent> eventHandler) {
        startInteractiveModeButton.setOnAction(eventHandler);
    }

    public SerialPort getSelectedSerialPort() {
        return portCombobox.getSelectionModel().getSelectedItem();
    }

    public void setSelectedSerialPort(SerialPort serialPort) {
        portCombobox.getSelectionModel().select(serialPort);
    }
}
