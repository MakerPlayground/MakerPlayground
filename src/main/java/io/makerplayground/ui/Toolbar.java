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

package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.ui.dialog.DeviceMonitor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class Toolbar extends HBox {

    @FXML private Button newButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private Button saveAsButton;
//    @FXML private MenuButton deviceMonitorMenuButton;
    @FXML private Label statusLabel;
//    @FXML private Button diagramEditorButton;

    public Toolbar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        deviceMonitorMenuButton.setOnAction(this::deviceMonitorMenuShowing);
    }

    public void setOnNewButtonPressed(EventHandler<ActionEvent> event) {
        newButton.setOnAction(event);
    }

    public void setOnLoadButtonPressed(EventHandler<ActionEvent> event) {
        loadButton.setOnAction(event);
    }

    public void setOnSaveButtonPressed(EventHandler<ActionEvent> event) {
        saveButton.setOnAction(event);
    }

    public void setOnSaveAsButtonPressed(EventHandler<ActionEvent> event) {
        saveAsButton.setOnAction(event);
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

//    private void deviceMonitorMenuShowing(Event e) {
//        MenuButton deviceMonitorButton = (MenuButton) e.getSource();
//        deviceMonitorButton.getItems().clear();
//        SerialPort[] commPorts = SerialPort.getCommPorts();
//        if (commPorts.length > 0) {
//            for (SerialPort port : commPorts) {
//                MenuItem item = new MenuItem(port.getDescriptivePortName());
//                // runLater to make sure that the menuitem is disappeared before open the DeviceMonitor
//                item.setOnAction(event -> Platform.runLater(() -> openDeviceMonitor(port.getSystemPortName())));
//                deviceMonitorButton.getItems().add(item);
//            }
//        } else {
//            MenuItem item = new MenuItem("No connected serial port found.\nPlease connect the board with computer.");
//            item.setDisable(true);
//            deviceMonitorButton.getItems().add(item);
//        }
//    }
//
//    private void openDeviceMonitor(String portName){
//        SerialPort port = SerialPort.getCommPort(portName);
//        //TODO: capture error in rare case the port is disconnected
//        DeviceMonitor deviceMonitor = new DeviceMonitor(port);
//        deviceMonitor.showAndWait();
//    }
}
