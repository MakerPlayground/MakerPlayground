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

package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.ui.canvas.helper.DynamicViewCreator;
import io.makerplayground.ui.canvas.helper.DynamicViewCreatorBuilder;
import io.makerplayground.ui.deprecated.DevicePanelIcon;
import io.makerplayground.ui.deprecated.DevicePanelIconViewModel;
import io.makerplayground.ui.dialog.devicepane.DeviceSelectorView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelView extends VBox {

    private final DevicePanelViewModel viewModel;

    @FXML private VBox devicePanel;
    @FXML private FlowPane sensorPane;
    @FXML private FlowPane actuatorPane;
    @FXML private FlowPane utilityPane;
    @FXML private FlowPane cloudPane;
    @FXML private FlowPane interfacePane;
    @FXML private FlowPane microcontrollerPane;

    @FXML public void onAddDeviceClick() {
        DeviceSelectorView deviceSelectorView = new DeviceSelectorView(getScene().getWindow());
        deviceSelectorView.show();
        deviceSelectorView.setOnHidden(event -> viewModel.addDevice(deviceSelectorView.getDeviceToBeAdded()));
    }

    public DevicePanelView(DevicePanelViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/devicepanel/DevicePanelView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

//        // initialize platform panel
//        VBox platformVBox = new VBox();
//        platformVBox.setSpacing(10);
//        ToggleGroup platformToggleGroup = new ToggleGroup();
//        for (Platform platform : Platform.values()) {
//            RadioButton radioButton  = new RadioButton(platform.getDisplayName());
//            radioButton.setUserData(platform);
//            radioButton.setToggleGroup(platformToggleGroup);
//            radioButton.setOnAction(event -> viewModel.selectedPlatformProperty().set(platform));
//            if (platform == viewModel.selectedPlatformProperty().get()) {
//                radioButton.setSelected(true);
//            }
//            platformVBox.getChildren().add(radioButton);
//        }
//        microcontrollerPane.getChildren().add(platformVBox);

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> inputViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                    .setParent(sensorPane)
                    .setModelLoader(viewModel.getSensorChildViewModel())
                    .setViewFactory(devicePanelIconViewModel -> {
                        DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                        icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
                        return icon;
                    })
                    .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                    .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                    .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                    .setParent(actuatorPane)
                    .setModelLoader(viewModel.getActuatorChildViewModel())
                    .setViewFactory(devicePanelIconViewModel -> {
                        DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                        icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
                        return icon;
                    })
                    .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                    .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                    .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> utilityViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                        .setParent(utilityPane)
                        .setModelLoader(viewModel.getUtilityChildViewModel())
                        .setViewFactory(devicePanelIconViewModel -> {
                            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
                            return icon;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> cloudViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                        .setParent(cloudPane)
                        .setModelLoader(viewModel.getCloudChildViewModel())
                        .setViewFactory(devicePanelIconViewModel -> {
                            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
                            return icon;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> interfaceViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                        .setParent(interfacePane)
                        .setModelLoader(viewModel.getInterfaceChildViewModel())
                        .setViewFactory(devicePanelIconViewModel -> {
                            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
                            return icon;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();
    }

}
