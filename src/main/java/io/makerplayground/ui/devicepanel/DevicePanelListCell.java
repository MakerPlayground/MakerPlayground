package io.makerplayground.ui.devicepanel;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class DevicePanelListCell extends ListCell<ProjectDevice> {

    private HBox hbox;
    private Label nameLabel;
    private TextField nameTextField;
    private Pane spacingPane;
    private Button deleteButton;

    public DevicePanelListCell(Project project) {
        super();
        setEditable(true);

        hbox = new HBox();

        nameLabel = new Label();
        nameLabel.managedProperty().bind(nameLabel.visibleProperty());
        nameLabel.setId("nameLabel");
        nameLabel.setTextFill(Color.WHITE);

        nameTextField = new TextField();
        nameTextField.setId("nameTextField");
        nameTextField.setVisible(false);
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

        spacingPane = new Pane();
        HBox.setHgrow(spacingPane, Priority.ALWAYS);

        deleteButton = new Button();
        deleteButton.setId("delButton");
        deleteButton.setOnAction(event -> {
            ProjectDevice projectDevice = getItem();
            project.removeSensor(projectDevice);
            project.removeConnectivity(projectDevice);
            project.removeActuator(projectDevice);
        });

        hbox.getChildren().addAll(nameLabel, nameTextField, spacingPane, deleteButton);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        nameLabel.setVisible(false);
        nameTextField.setVisible(true);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        nameLabel.setVisible(true);
        nameTextField.setVisible(false);
    }

    @Override
    protected void updateItem(ProjectDevice item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            nameLabel.setText(item.getName());
            setGraphic(hbox);
        }
    }
}
