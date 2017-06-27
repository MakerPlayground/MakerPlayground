package io.makerplayground.ui;

import io.makerplayground.project.ProjectDevice;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceIconSelectorView extends VBox {
    ProjectDevice projectDevice;

    public OutputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;

        ImageView imv = new ImageView();
        Image image = new Image(Main.class.getResourceAsStream("/icons/" + projectDevice.getGenericDevice().getName() + ".png"));
        imv.setImage(image);
        imv.setFitHeight(50);
        imv.setPreserveRatio(true);

        Label name = new Label(projectDevice.getName());
        getChildren().addAll(imv, name);
    }

}
