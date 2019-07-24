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

package io.makerplayground.ui.dialog.generate;

import io.makerplayground.project.ProjectDevice;
import javafx.scene.control.Hyperlink;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TableDataList {
    private final String name;
    private final String brand;
    private final String model;
    private final String id;
    private final String pin;
    private final String url;

//    public TableDataList(ProjectDevice projectDevice) {
//        ProjectDevice actualDevice;
//        if (projectDevice.isMergeToOtherDevice()) {
//            actualDevice = projectDevice.getIdenticalDevice();
//        } else {
//            actualDevice = projectDevice;
//        }
//
//        this.name = projectDevice.getName();
//        this.brand = actualDevice.getCompatibleDeviceComboItem().getBrand();
//        this.model = actualDevice.getCompatibleDeviceComboItem().getModel();
//        this.id = actualDevice.getCompatibleDeviceComboItem().getId();
//        List<String> list = new ArrayList<>();
//
//        /* TODO: uncomment this */
////        for (Peripheral p : actualDevice.getCompatibleDeviceComboItem().getConnectivity()) {
////            if (p.getConnectionType() != ConnectionType.I2C) {
////                List<DevicePort> port = actualDevice.getDeviceConnection().get(p);
////                if (port == null) {
////                    throw new IllegalStateException("Port hasn't been selected!!!");
////                }
////                list.addAll(port.stream().map(DevicePort::getName).collect(Collectors.toList()));
////            } else { //i2c and others
////                List<DevicePort> port = actualDevice.getDeviceConnection().get(p);
////                if (port == null) {
////                    throw new IllegalStateException("Port hasn't been selected!!!");
////                }
////                list.addAll(port.stream().map(DevicePort::getName).collect(Collectors.toList()));
////            }
////        }
//        this.pin = String.join(",", list);
//        this.url = actualDevice.getCompatibleDeviceComboItem().getUrl();
//    }

    public Hyperlink getHyperLink() {
        Hyperlink link = new Hyperlink();
//        link.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//                SingletonDeviceURL.getInstance().setAll(url);
//                Desktop desktop = Desktop.getDesktop();
//                try {
//                    desktop.browse(URI.create(url));
//                } catch (IOException ev) {
//                    ev.printStackTrace();
//                }
//            }
//        });
        link.setText(url);
        return link;
    }

}
