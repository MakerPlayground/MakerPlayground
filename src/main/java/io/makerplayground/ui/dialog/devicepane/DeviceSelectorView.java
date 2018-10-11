/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.dialog.devicepane;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.ui.dialog.UndecoratedDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorView extends UndecoratedDialog {
    private AnchorPane anchorPane = new AnchorPane();
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private ImageView closeButton;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane inputPane;
    @FXML private FlowPane virtualPane;

    private Map<GenericDevice, Integer> deviceToBeAdded = new HashMap<>();

    private ObservableList<ControlAddDevicePane> outputDevice;
    private ObservableList<ControlAddDevicePane> inputDevice;
    private ObservableList<ControlAddDevicePane> virtualDevice;

    public DeviceSelectorView(Window owner) {
        super(owner);

        this.inputDevice = FXCollections.observableArrayList();
        this.outputDevice = FXCollections.observableArrayList();
        this.virtualDevice = FXCollections.observableArrayList();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/DeviceSelectorView.fxml"));
        fxmlLoader.setRoot(anchorPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        initView();
        initEvent();
        setContent(anchorPane);
    }

    private void initView() {
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericOutputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            outputPane.getChildren().add(controlDevicePane);
            this.outputDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            inputPane.getChildren().add(controlDevicePane);
            this.inputDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericConnectivityDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            virtualPane.getChildren().add(controlDevicePane);
            this.virtualDevice.add(controlDevicePane);
        }
    }

    private void initEvent() {
        okButton.setOnAction(event -> {
            for (ControlAddDevicePane d : outputDevice) {
                deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
            }
            for (ControlAddDevicePane d : inputDevice) {
                deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
            }
            for (ControlAddDevicePane d : virtualDevice) {
                deviceToBeAdded.put(d.getGenericDevice(), d.getCount());
            }
            hide();
        });

        cancelButton.setOnAction(event -> hide());
        closeButton.setOnMouseReleased(event -> hide());
    }

    public Map<GenericDevice, Integer> getDeviceToBeAdded() {
        return Collections.unmodifiableMap(deviceToBeAdded);
    }
}