package io.makerplayground.ui.canvas.devicepane.input;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.Main;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceIconSelectorView extends  VBox {
    ProjectDevice projectDevice;

    public InputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;

        ImageView imv = new ImageView();
        Image image = new Image(Main.class.getResourceAsStream("/icons/colorIcons/" + projectDevice.getGenericDevice().getName() + ".png"));
        imv.setImage(image);
        imv.setFitHeight(50);
        imv.setPreserveRatio(true);
        setAlignment(Pos.CENTER);
        setMinSize(50,70);

        Label name = new Label(projectDevice.getName());
        name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        name.setStyle("-fx-font-size: 12px;");
        name.setMinWidth(70);
        name.setMaxWidth(70);
        name.setAlignment(Pos.CENTER);
        getChildren().addAll(imv, name);
    }
}
