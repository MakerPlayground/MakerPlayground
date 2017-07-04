package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

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
        setTitle("Device Library");
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