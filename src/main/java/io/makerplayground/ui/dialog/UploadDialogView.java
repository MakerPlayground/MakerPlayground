package io.makerplayground.ui.dialog;

import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonError;
import io.makerplayground.helper.UploadResult;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;

public class UploadDialogView extends UndecoratedDialog {
    private final VBox vbox = new VBox();
    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea textArea;
    @FXML private ImageView imgView;
    @FXML private TitledPane detailPane;

    private final StringProperty logProperty;

    public UploadDialogView(Window owner, UploadTask uploadTask) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/UploadDialogView.fxml"));
        fxmlLoader.setRoot(vbox);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        RotateTransition rt = new RotateTransition();
        rt.setNode(imgView);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();

        // Ask for confirmation to cancel when user close this dialog before upload complete
        setClosingPredicate(() -> {
            if (uploadTask.isDone()) {
                return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK){
                    uploadTask.cancel();
                }
                return false;
            }
        });

        // Cancel the rotation effect when the upload task is cancelled
        uploadTask.setOnCancelled(event -> {
            rt.stop();
        });

        // Change image to success or error
        uploadTask.setOnSucceeded(event1 -> {
            UploadResult result = uploadTask.getValue();
            if (result == UploadResult.OK) {
                imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Success.png")));
                rt.stop();
                imgView.setRotate(0);
            } else {
                imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Error-uploading.png")));
                progress.setTextFill(Color.RED);
                detailPane.setExpanded(true);
                SingletonError.getInstance().setAll(progress.getText());
                rt.stop();
            }
        });

        // append text to the textarea when new log is coming
        logProperty = new SimpleStringProperty();
        logProperty.addListener((observable, oldValue, newValue) -> {
            textArea.appendText(newValue);
        });

        // resize the dialog after the detailPane is expanded or collapsed
        detailPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            // expandedProperty() changed before the pane is actually expanded or collapsed thus sizeToScene() is executed
            // based on the old (incorrect) size so we wrap it in Plaform.runLater() to put it in an event queue after the
            // pane is actually expanded or collapsed (this may be unsafe and may break in different jdk!!!)
            Platform.runLater(this::sizeToScene);
        });

        setContent(vbox);
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
