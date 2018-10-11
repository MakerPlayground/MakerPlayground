/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class UploadDialogView extends UndecoratedDialog {
    private final AnchorPane anchorPane = new AnchorPane();
    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea textArea;
    @FXML private ImageView imgView;
    @FXML private TitledPane detailPane;
    @FXML private ImageView closeButton;

    private final StringProperty logProperty;

    public UploadDialogView(Window owner, UploadTask uploadTask) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/UploadDialogView.fxml"));
        fxmlLoader.setRoot(anchorPane);
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
        BooleanSupplier closingConfirm = () -> {
            if (uploadTask.isDone()) {
                return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    uploadTask.cancel();
                }
                return false;
            }
        };
        setClosingPredicate(closingConfirm);

        // allow the dialog to be closed with the close button at the top right corner
        closeButton.setOnMouseReleased(event -> {
            if (closingConfirm.getAsBoolean()) {
                hide();
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

        setContent(anchorPane);
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
