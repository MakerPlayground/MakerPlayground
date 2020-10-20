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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class DeviceLibraryErrorDialogView extends UndecoratedDialog {

    private final AnchorPane mainPane = new AnchorPane();
    @FXML private TitledPane detailPane;
    @FXML private TreeView<String> detailTreeView;

    public DeviceLibraryErrorDialogView(Window owner, Map<Path, String> errors) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/DeviceLibraryErrorDialogView.fxml"));
        fxmlLoader.setRoot(mainPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        TreeItem<String> rootTreeItem = new TreeItem<>();
        for (Path path : errors.keySet()) {
            TreeItem<String> errorMessageTreeItem = new TreeItem<>(errors.get(path));
            TreeItem<String> filenameTreeItem = new TreeItem<>(path.toAbsolutePath().toString());
            filenameTreeItem.setExpanded(true);
            filenameTreeItem.getChildren().add(errorMessageTreeItem);
            rootTreeItem.getChildren().add(filenameTreeItem);
        }
        detailTreeView.setFocusTraversable(false);  // eliminate blue highlight when selecting row in the TreeView
        detailTreeView.setRoot(rootTreeItem);
        detailTreeView.setShowRoot(false);

        // resize the dialog after the detailPane is expanded or collapsed
        detailPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            // expandedProperty() changed before the pane is actually expanded or collapsed thus sizeToScene() is executed
            // based on the old (incorrect) size so we wrap it in Plaform.runLater() to put it in an event queue after the
            // pane is actually expanded or collapsed (this may be unsafe and may break in different jdk!!!)
            Platform.runLater(this::sizeToScene);
        });

        setContent(mainPane);
    }
}
