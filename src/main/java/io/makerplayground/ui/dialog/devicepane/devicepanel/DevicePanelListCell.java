package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.io.IOException;

public class DevicePanelListCell extends ListCell<ProjectDevice> {

    @FXML private HBox hbox;
    @FXML private TextField nameTextField;
    @FXML private Pane spacingPane;
    @FXML private Button deleteButton;
    private Project project;

    public DevicePanelListCell(Project project) {
        super();

        this.project = project;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/devicepanel/DevicePanelListCell.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        nameTextField.managedProperty().bind(nameTextField.visibleProperty());
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (!project.isNameDuplicate(nameTextField.getText())) {
                    getItem().setName(nameTextField.getText());
                } else {
                    nameTextField.setText(getItem().getName());
                }
            }
        });
    }

    @Override
    protected void updateItem(ProjectDevice item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            nameTextField.setText(item.getName());
            setGraphic(hbox);
        }
    }

    private void deleteHandler() {
        ProjectDevice projectDevice = getItem();
        project.removeSensor(projectDevice);
        project.removeConnectivity(projectDevice);
        project.removeActuator(projectDevice);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        deleteHandler();
    }



}
