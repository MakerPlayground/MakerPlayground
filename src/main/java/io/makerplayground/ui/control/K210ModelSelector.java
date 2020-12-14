package io.makerplayground.ui.control;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.UploadDialogView;
import io.makerplayground.upload.K210ObjectDetectionModelFlashTask;
import io.makerplayground.upload.UploadTarget;
import io.makerplayground.upload.UploadTask;
import io.makerplayground.device.shared.K210ObjectDetectionModel;
import io.makerplayground.util.PathUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class K210ModelSelector extends VBox {

    private final ActualDevice actualDevice;
    private final ReadOnlyObjectWrapper<K210ObjectDetectionModel> value;

    public K210ModelSelector(Project project, ActualDevice actualDevice, K210ObjectDetectionModel currentValue, ReadOnlyObjectProperty<UploadTarget> uploadTarget) {
        this.actualDevice = actualDevice;
        this.value = new ReadOnlyObjectWrapper<>(currentValue);

        ComboBox<K210ObjectDetectionModel> comboBox = new ComboBox<>(FXCollections.observableList(PathUtility.getDeviceModelPath(actualDevice, K210ObjectDetectionModel.class)));
        comboBox.setCellFactory(param -> new ListCell<>(){
            @Override
            protected void updateItem(K210ObjectDetectionModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        comboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(K210ObjectDetectionModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        if (currentValue != null) {
            comboBox.getSelectionModel().select(currentValue);
        }
        this.value.bind(comboBox.valueProperty());

        Label classLabel = new Label();
        classLabel.setWrapText(true);
        classLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (this.value.get() == null) {
                return "Available classes: -";
            } else {
                return "Available classes: " + String.join(", ", this.value.get().getClassName());
            }
        }, this.value));

        Button flashButton = new Button("Upload to board");
        flashButton.disableProperty().bind(comboBox.valueProperty().isNull());
        flashButton.setOnAction((event) -> {
            if (comboBox.getValue() == null) {
                return;
            }
            UploadTask flashTask = new K210ObjectDetectionModelFlashTask(project, uploadTarget.get(), actualDevice, comboBox.getValue());
            UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), flashTask, false);
            uploadDialogView.progressProperty().bind(flashTask.progressProperty());
            uploadDialogView.descriptionProperty().bind(flashTask.messageProperty());
            uploadDialogView.logProperty().bind(flashTask.fullLogProperty());
            uploadDialogView.show();
            new Thread(flashTask).start();
        });

        setSpacing(5);
        getChildren().addAll(comboBox, classLabel, flashButton);
    }

    public ReadOnlyObjectProperty<K210ObjectDetectionModel> selectedModelProperty() {
        return value.getReadOnlyProperty();
    }
}
