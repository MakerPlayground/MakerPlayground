package io.makerplayground.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class DeviceJsonEditorView extends BorderPane {
    DeviceJsonEditorViewModel deviceJsonEditorView;

    public DeviceJsonEditorView(DeviceJsonEditorViewModel deviceJsonEditorViewModel) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceJsonEditorView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.deviceJsonEditorView = deviceJsonEditorViewModel;
    }
}
