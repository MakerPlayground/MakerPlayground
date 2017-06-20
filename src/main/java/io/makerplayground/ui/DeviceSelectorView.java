package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorView extends Dialog {

    //private final DeviceSelectorViewModel viewModel;\
    private ObservableList<ControlAddDevicePane> outputDevice;

    public DeviceSelectorView() {
        this.outputDevice = FXCollections.observableArrayList();
        initView();
    }

    private void initView() {
        setTitle("Add Device");

        VBox vbox = new VBox();

        ScrollPane mcuScroll = new ScrollPane();
        ScrollPane outputScroll = new ScrollPane();
        ScrollPane inputScroll = new ScrollPane();
        FlowPane mcuPane = new FlowPane();
        FlowPane outputPane = new FlowPane();

        for (GenericDevice d  : DeviceLibrary.INSTANCE.getOutputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            outputPane.getChildren().add(controlDevicePane);
            this.outputDevice.add(controlDevicePane);
        }
        outputPane.setHgap(10);

        FlowPane inputPane = new FlowPane();
        mcuScroll.setContent(mcuPane);
        outputScroll.setContent(outputPane);
        inputScroll.setContent(inputPane);

        TitledPane t1 = new TitledPane("Microcontroller", mcuScroll);
        TitledPane t2 = new TitledPane("Output Device", outputScroll);
        TitledPane t3 = new TitledPane("Input Device", inputScroll);
        vbox.getChildren().addAll(t1, t2, t3);

        getDialogPane().setContent(vbox);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

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