package io.makerplayground.ui;

import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;

public class ProjectDevicePanel extends TabPane {
    @FXML private TitledPane actuatorTitledPane;
    @FXML private VBox actuatorVBox;
    @FXML private TitledPane sensorTitledPane;
    @FXML private VBox sensorVBox;
    @FXML private TitledPane utilityTitledPane;
    @FXML private VBox utilityVBox;
    @FXML private TitledPane cloudTitledPane;
    @FXML private VBox cloudVBox;
    @FXML private TitledPane interfaceTitledPane;
    @FXML private VBox interfaceVBox;
    @FXML private VBox warningPane;

    private final Project project;
    private final FilteredList<ProjectDevice> actuatorList;
    private final FilteredList<ProjectDevice> sensorList;
    private final FilteredList<ProjectDevice> utilityList;
    private final FilteredList<ProjectDevice> cloudList;
    private final FilteredList<ProjectDevice> interfaceList;

    public ProjectDevicePanel(Project project) {
        this.project = project;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ProjectDevicePanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        actuatorList = project.getUnmodifiableProjectDevice().filtered(projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.ACTUATOR);
        sensorList = project.getUnmodifiableProjectDevice().filtered(projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.SENSOR);
        utilityList = project.getUnmodifiableProjectDevice().filtered(projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.UTILITY);
        cloudList = project.getUnmodifiableProjectDevice().filtered(projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.CLOUD);
        interfaceList = project.getUnmodifiableProjectDevice().filtered(projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.INTERFACE);

        initView(actuatorVBox, actuatorList);
        initView(sensorVBox, sensorList);
        initView(utilityVBox, utilityList);
        initView(cloudVBox, cloudList);
        initView(interfaceVBox, interfaceList);

        actuatorTitledPane.visibleProperty().bind(Bindings.isNotEmpty(actuatorList));
        actuatorTitledPane.managedProperty().bind(actuatorTitledPane.visibleProperty());
        sensorTitledPane.visibleProperty().bind(Bindings.isNotEmpty(sensorList));
        sensorTitledPane.managedProperty().bind(sensorTitledPane.visibleProperty());
        utilityTitledPane.visibleProperty().bind(Bindings.isNotEmpty(utilityList));
        utilityTitledPane.managedProperty().bind(utilityTitledPane.visibleProperty());
        cloudTitledPane.visibleProperty().bind(Bindings.isNotEmpty(cloudList));
        cloudTitledPane.managedProperty().bind(cloudTitledPane.visibleProperty());
        interfaceTitledPane.visibleProperty().bind(Bindings.isNotEmpty(interfaceList));
        interfaceTitledPane.managedProperty().bind(interfaceTitledPane.visibleProperty());
        warningPane.visibleProperty().bind(Bindings.isEmpty(project.getUnmodifiableProjectDevice()));
        warningPane.managedProperty().bind(warningPane.visibleProperty());
    }

    private void initView(VBox parent, ObservableList<ProjectDevice> model) {
        model.forEach(projectDevice -> parent.getChildren().add(new ProjectDeviceListCell(projectDevice)));
        model.addListener((ListChangeListener<ProjectDevice>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new IllegalStateException();
                } else if (c.wasUpdated()) {
                    throw new IllegalStateException();
                } else {
                    for (ProjectDevice projectDevice : c.getRemoved()) {
                        parent.getChildren().removeIf(node -> ((ProjectDeviceListCell) node).getProjectDevice() == projectDevice);
                    }
                    for (ProjectDevice projectDevice : c.getAddedSubList()) {
                        parent.getChildren().add(new ProjectDeviceListCell(projectDevice));
                    }
                }
            }
        });
    }

    private final class ProjectDeviceListCell extends HBox {
        @FXML private ImageView deviceIcon;
        @FXML private TextField nameTextField;
        @FXML private Button removeButton;

        private final ProjectDevice projectDevice;

        public ProjectDeviceListCell(ProjectDevice projectDevice) {
            this.projectDevice = projectDevice;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ProjectDeviceListCell.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (InputStream imageStream = getClass().getResourceAsStream("/icons/colorIcons-3/" + projectDevice.getGenericDevice().getName() + ".png")) {
                deviceIcon.setImage(new Image(imageStream));
            } catch (NullPointerException | IOException e) {
                throw new IllegalStateException("Missing icon of " + projectDevice.getGenericDevice().getName());
            }
            nameTextField.setText(projectDevice.getName());
            nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    String newName = nameTextField.getText();
                    if (!newName.matches("^[a-zA-Z_][a-zA-Z0-9_]*") || project.isNameDuplicate(newName)){
                        nameTextField.setText(projectDevice.getName());
                    } else {
                        projectDevice.setName(newName);
                    }
                }
            });
            removeButton.setOnAction(event -> project.removeDevice(projectDevice));
        }

        public ProjectDevice getProjectDevice() {
            return projectDevice;
        }
    }
}
