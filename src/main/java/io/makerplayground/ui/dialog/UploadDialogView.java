package io.makerplayground.ui.dialog;

import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonError;
import io.makerplayground.helper.UploadResult;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

public class UploadDialogView extends Dialog {
    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea textArea;
    @FXML private ImageView imgView;
    @FXML private TitledPane detailPane;

    private final StringProperty logProperty;

    public UploadDialogView(UploadTask uploadTask) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/UploadDialogView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setTitle("  Upload");
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);

        // Ask for confirmation to cancel when user close this dialog before upload complete
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            if (uploadTask.isDone()) {
                window.hide();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK){
                    uploadTask.cancel();
                }
            }
            event.consume();
        });

        // Change image to success or error
        uploadTask.setOnSucceeded(event1 -> {
            UploadResult result = uploadTask.getValue();
            if (result == UploadResult.OK) {
                imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Success.png")));
            } else {
                progress.setTextFill(Color.RED);
                detailPane.setExpanded(true);
                SingletonError.getInstance().setAll(progress.getText());
            }
        });

        // append text to the textarea when new log is coming
        logProperty = new SimpleStringProperty();
        logProperty.addListener((observable, oldValue, newValue) -> {
            textArea.appendText(newValue);
        });
    }

    public StringProperty descriptionProperty() {
        return progress.textProperty();
    }

    public DoubleProperty progressProperty() {
        return progressBar.progressProperty();
    }

    public StringProperty logProperty() {
        return logProperty;
    }

}
