package io.makerplayground.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
        removedButton.setPrefSize(15,15);

        AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setRightAnchor(removedButton,0.0);
        AnchorPane.setTopAnchor(removedButton,0.0);
        anchorPane.setPrefSize(50,50);

        Image deviceImg = new Image(getClass().getResourceAsStream("/icons/" + viewModel.getDeviceName() + ".png"));
        ImageView deviceImgView = new ImageView(deviceImg);
        deviceImgView.setFitHeight(50);
        deviceImgView.setPreserveRatio(true);

        StackPane pane = new StackPane();
        pane.getChildren().add(deviceImgView);
        pane.setPrefHeight(50.0);
        pane.setPrefWidth(50.0);
        StackPane.setAlignment(deviceImgView, Pos.CENTER);
        anchorPane.getChildren().addAll(pane,removedButton);
        // TODO: Change it to an editable label

        TextField name = new TextField();
        name.textProperty().bindBidirectional(viewModel.nameProperty());
        name.setPrefWidth(50.0);
        setSpacing(5.0);
        getChildren().addAll(anchorPane,name);
    }

    public void setOnAction(EventHandler<ActionEvent> e) {
        removedButton.setOnAction(e);
    }
}
