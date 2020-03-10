/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

package io.makerplayground.ui.deprecated.devicepane;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
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
import java.util.*;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
@Deprecated
public class DeviceSelectorView extends UndecoratedDialog {
    private AnchorPane anchorPane = new AnchorPane();
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private ImageView closeButton;
    @FXML private FlowPane actuatorPane;
    @FXML private FlowPane sensorPane;
    @FXML private FlowPane utilityPane;
    @FXML private FlowPane cloudPane;
    @FXML private FlowPane interfacePane;

    private Map<GenericDevice, Integer> deviceToBeAdded = new HashMap<>();

    private ObservableList<ControlAddDevicePane> allDevice = FXCollections.observableArrayList();

    public DeviceSelectorView(Window owner) {
        super(owner);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/deprecated/devicepane/DeviceSelectorView.fxml"));
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
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericActuatorDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            actuatorPane.getChildren().add(controlDevicePane);
            this.allDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericSensorDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            sensorPane.getChildren().add(controlDevicePane);
            this.allDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericUtilityDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            utilityPane.getChildren().add(controlDevicePane);
            this.allDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericCloudDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            cloudPane.getChildren().add(controlDevicePane);
            this.allDevice.add(controlDevicePane);
        }
        for (GenericDevice d  : DeviceLibrary.INSTANCE.getGenericInterfaceDevice()) {
            ControlAddDevicePane controlDevicePane = new ControlAddDevicePane(d);
            interfacePane.getChildren().add(controlDevicePane);
            this.allDevice.add(controlDevicePane);
        }
    }

    private void initEvent() {
        okButton.setOnAction(event -> {
            allDevice.forEach(d->deviceToBeAdded.put(d.getGenericDevice(), d.getCount()));
            hide();
        });
        cancelButton.setOnAction(event -> hide());
        closeButton.setOnMouseReleased(event -> hide());
    }

    public Map<GenericDevice, Integer> getDeviceToBeAdded() {
        return Collections.unmodifiableMap(deviceToBeAdded);
    }
}