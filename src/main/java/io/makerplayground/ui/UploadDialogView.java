package io.makerplayground.ui;

import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.UploadResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;

public class UploadDialogView extends Dialog {
    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea textArea;
    @FXML private ImageView imgView;
    @FXML private TitledPane detailPane;

    public UploadDialogView(UploadTask uploadTask) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/UploadDialogView.fxml"));
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

        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            if (uploadTask.isDone())
                window.hide();
        });

        // Auto close if there is no error, otherwise we keep it open to allow user to see error message
        uploadTask.setOnSucceeded(event1 -> {
            UploadResult result = uploadTask.getValue();
            if (result == UploadResult.OK) {
                imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Success.png")));
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),
                        event2 -> getDialogPane().getScene().getWindow().hide()));
                timeline.play();
            } else {
                //okButton.setDisable(false);
                detailPane.setExpanded(true);
                //dialog.getDialogPane().lookupButton(buttonType).setDisable(false);
            }
        });
    }

    public StringProperty descriptionProperty() {
        return progress.textProperty();
    }

    public DoubleProperty progressProperty() {
        return progressBar.progressProperty();
    }

    public StringProperty logProperty() {
        return textArea.textProperty();
    }

}
