package io.makerplayground.ui.dialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;

public class ErrorDialogView extends UndecoratedDialog {

    private VBox mainPane = new VBox();
    @FXML private Label descriptionLabel;

    public ErrorDialogView(Window owner, String error) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/ErrorDialogView.fxml"));
        fxmlLoader.setRoot(mainPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        descriptionLabel.setText(error);
        setContent(mainPane);
    }
}
