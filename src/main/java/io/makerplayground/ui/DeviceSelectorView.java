package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorView extends Dialog {
    @FXML private DialogPane deviceSelectorPane;
    @FXML private FlowPane mcuPane;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane inputPane;
    //private final DeviceSelectorViewModel viewModel;\
    private ObservableList<ControlAddDevicePane> outputDevice;

    public DeviceSelectorView() {
        this.outputDevice = FXCollections.observableArrayList();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceSelectorView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        initView();
    }

    private void initView() {
        setTitle("Add Device");
        getDialogPane().setExpanded(true);
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getOutputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            outputPane.getChildren().add(controlDevicePane);
            this.outputDevice.add(controlDevicePane);
        }
        ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == importButtonType) {
                for (ControlAddDevicePane d : outputDevice) {
                    System.out.println(d.getGenericDevice().getName() + " " + d.getCount());
                }
                return outputDevice;
            }
            return null;
        });
    }
}