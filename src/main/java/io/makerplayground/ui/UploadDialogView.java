package io.makerplayground.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class UploadDialogView extends Dialog {
    @FXML
    private Label descriptionLabel;
    @FXML
    public ImageView imgView;

    public UploadDialogView() {
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

        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }

    public void setDescriptionLabel(String descriptionLabel) {
        //this.descriptionLabel.setText(descriptionLabel);
    }

    public Label getDescriptionLabel() {
        return descriptionLabel;
    }

}
