/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;

public class UnsavedDialog extends UndecoratedDialog {

    public enum Response {
        SAVE, DONT_SAVE, CANCEL
    }

    private VBox mainPane = new VBox();
    @FXML private Label descriptionLabel;
    @FXML private Button saveButton;
    @FXML private Button notSaveButton;
    @FXML private Button cancelButton;
    @FXML private ImageView closeButton;

    private Response response = Response.CANCEL;

    public UnsavedDialog(Window owner/*, String error*/) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/UnsavedDialog.fxml"));
        fxmlLoader.setRoot(mainPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        saveButton.setOnAction(event -> {
            response = Response.SAVE;
            hide();
        });

        notSaveButton.setOnAction(event -> {
            response = Response.DONT_SAVE;
            hide();
        });

        cancelButton.setOnAction(event -> {
            response = Response.CANCEL;
            hide();
        });

        closeButton.setOnMouseReleased(event -> {
            response = Response.CANCEL;
            hide();
        });

        setContent(mainPane);
    }

    public Response showAndGetResponse() {
        showAndWait();
        return response;
    }
}

