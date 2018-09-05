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

    private Response response;

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

