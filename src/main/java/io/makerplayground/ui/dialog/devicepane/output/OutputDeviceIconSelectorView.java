package io.makerplayground.ui.dialog.devicepane.output;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.Main;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceIconSelectorView extends VBox {
    ProjectDevice projectDevice;

    public OutputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;

        ImageView imv = new ImageView();
        Image image = new Image(Main.class.getResourceAsStream("/icons/darktheme/" + projectDevice.getGenericDevice().getName() + ".png"));
        imv.setImage(image);
        imv.setFitHeight(50);
        imv.setPreserveRatio(true);
        setAlignment(Pos.CENTER);
        setMinSize(50,70);

        Label name = new Label(projectDevice.getName());
        name.setMinWidth(70);
        name.setAlignment(Pos.CENTER);
        getChildren().addAll(imv, name);
    }
}
