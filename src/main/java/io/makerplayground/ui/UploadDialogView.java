package io.makerplayground.ui;

import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonError;
import io.makerplayground.helper.UploadResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
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

    private StringProperty logProperty;

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

//        textArea.textProperty().addListener(new ChangeListener<Object>() {
//            @Override
//            public void changed(ObservableValue<?> observable, Object oldValue,
//                                Object newValue) {
//                textArea.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
//                //use Double.MIN_VALUE to scroll to the top
//            }
//        });
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
//                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),
//                        event2 -> getDialogPane().getScene().getWindow().hide()));
//                timeline.play();
            } else {
                //okButton.setDisable(false);
                progress.setTextFill(Color.RED);
                detailPane.setExpanded(true);
                SingletonError.getInstance().setAll(progress.getText());
                //dialog.getDialogPane().lookupButton(buttonType).setDisable(false);
            }
        });

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
