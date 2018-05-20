package io.makerplayground.ui.canvas;

import io.makerplayground.ui.Main;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class TimerIconView extends VBox {

    public TimerIconView() {
        ImageView imv = new ImageView();
        Image image = new Image(Main.class.getResourceAsStream("/icons/colorIcons/Time.png"));
        imv.setImage(image);
        imv.setFitHeight(50);
        imv.setPreserveRatio(true);
        setAlignment(Pos.CENTER);
        setMinSize(50,70);

        Label name = new Label("Time");
        name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        name.setStyle("-fx-font-size: 12px;");
        name.setMinWidth(70);
        name.setMaxWidth(70);
        name.setAlignment(Pos.CENTER);
        getChildren().addAll(imv, name);
    }
}
