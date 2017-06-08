package io.makerplayground.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIcon extends VBox {

    private final DevicePanelIconViewModel viewModel;
    private Button removedButton;

    public DevicePanelIcon(DevicePanelIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        removedButton = new Button("x");

        Image deviceImg = new Image(getClass().getResourceAsStream("/icons/" + viewModel.getDeviceName() + ".png"));
        ImageView deviceImgView = new ImageView(deviceImg);
        deviceImgView.setFitHeight(50);
        deviceImgView.setPreserveRatio(true);

        // TODO: Change it to an editable label
        TextField name = new TextField();
        name.textProperty().bindBidirectional(viewModel.nameProperty());

        getChildren().addAll(removedButton, deviceImgView, name);
    }

    public void setOnAction(EventHandler<ActionEvent> e) {
        removedButton.setOnAction(e);
    }
}
