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

import io.makerplayground.generator.upload.UploadResult;
import io.makerplayground.generator.upload.UploadTask;
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

public class UploadDialogView extends UndecoratedDialog {
    private final AnchorPane anchorPane = new AnchorPane();
    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea textArea;
    @FXML private ImageView imgView;
    @FXML private TitledPane detailPane;
//    @FXML private ImageView closeButton;

    private final UploadTask uploadTask;
    private final RotateTransition rt;
    private final StringProperty logProperty;

    public UploadDialogView(Window owner, UploadTask uploadTask) {
        super(owner);
        this.uploadTask = uploadTask;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/UploadDialogView.fxml"));
        fxmlLoader.setRoot(anchorPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        rt = new RotateTransition();
        rt.setNode(imgView);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);

        // initialize ui based on the current state of the task
        if (uploadTask.isRunning()) {
            rt.play();
        } else if (uploadTask.isDone()) {
            updateUI();
        }

        // Cancel the rotation effect when the upload task is cancelled
        uploadTask.setOnCancelled(event -> {
            rt.stop();
        });

        // Change image to success or error
        uploadTask.setOnSucceeded(event1 -> {
            updateUI();
        });

        // update text in the textarea when log is updated
        logProperty = new SimpleStringProperty();
        logProperty.addListener((observable, oldValue, newValue) -> {
            textArea.setText(newValue);
            textArea.setScrollTop(Double.MAX_VALUE);
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

    private void updateUI() {
        UploadResult result = uploadTask.getValue();
        if (result == UploadResult.OK) {
            imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Success.png")));
            rt.stop();
            imgView.setRotate(0);
        } else {
            imgView.setImage(new Image(getClass().getResourceAsStream("/icons/Error-uploading.png")));
            progress.setTextFill(Color.RED);
            detailPane.setExpanded(true);
            rt.stop();
        }
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
