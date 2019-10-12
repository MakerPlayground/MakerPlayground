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

package io.makerplayground.ui;

import io.makerplayground.version.SoftwareVersion;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class UpdateNotifier extends Thread {

    private final Window window;
    private final HostServices hostServices;

    public UpdateNotifier(Window window, HostServices hostServices) {
        this.window = window;
        this.hostServices = hostServices;
    }

    @Override
    public void run() {
        SoftwareVersion.getLatestVersionInfo().ifPresent(version -> {
            if (version.compareTo(SoftwareVersion.getCurrentVersion()) > 0) {
                Platform.runLater(() -> {
                    ImageView icon = new ImageView(new Image(getClass().getResource("/icons/download-2.png").toExternalForm()));
                    icon.setFitWidth(50);
                    icon.setPreserveRatio(true);

                    Text text = new Text(version.getBuildName() + " has been released");
                    text.setId("text");
                    text.setWrappingWidth(250);

                    Button button = new Button("Download Now");
                    button.setId("UpdateButton");
                    button.setOnAction(event -> hostServices.showDocument(version.getDownloadURL()));

                    VBox vBox = new VBox();
                    vBox.setSpacing(20);
                    vBox.setAlignment(Pos.TOP_CENTER);
                    vBox.getChildren().addAll(text, button);

                    HBox mainPane = new HBox();
                    mainPane.setPadding(new Insets(10));
                    mainPane.setSpacing(20);
                    mainPane.getStylesheets().add(getClass().getResource("/css/UpdateNotificationDialog.css").toExternalForm());
                    mainPane.setPrefSize(300, 80);
                    mainPane.getChildren().addAll(icon, vBox);

                    Notifications.create()
                            .graphic(mainPane)
                            .owner(window)
                            .hideAfter(Duration.seconds(5))
                            .show();
                });
            }
        });
    }
}
