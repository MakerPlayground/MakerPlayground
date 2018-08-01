package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.device.GenericDevice;

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
import java.util.Map;
import java.util.Optional;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelView extends VBox {

    private final DevicePanelViewModel viewModel;

    @FXML
    private VBox devicePanel;
    @FXML private FlowPane inputPane;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane connectivityPane;
    @FXML private FlowPane microcontrollerPane;

    @FXML public void onAddDeviceClick() {
        DeviceSelectorView deviceSelectorView = new DeviceSelectorView();
        Optional<Map<GenericDevice, Integer>> result = deviceSelectorView.showAndWait();
        result.ifPresent(viewModel::addDevice);
    }

    public DevicePanelView(DevicePanelViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DevicePanelView.fxml"));
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
                    .setParent(inputPane)
                    .setModelLoader(viewModel.getInputChildViewModel())
                    .setViewFactory(devicePanelIconViewModel -> {
                        DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                        icon.setOnAction(event -> viewModel.removeInputDevice(devicePanelIconViewModel));
                        return icon;
                    })
                    .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                    .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                    .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                    .setParent(outputPane)
                    .setModelLoader(viewModel.getOutputChildViewModel())
                    .setViewFactory(devicePanelIconViewModel -> {
                        DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                        icon.setOnAction(event -> viewModel.removeOutputDevice(devicePanelIconViewModel));
                        return icon;
                    })
                    .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                    .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                    .createDynamicViewCreator();

        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> connectivityViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, DevicePanelIconViewModel, DevicePanelIcon>()
                        .setParent(connectivityPane)
                        .setModelLoader(viewModel.getConnectivityChildViewModel())
                        .setViewFactory(devicePanelIconViewModel -> {
                            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
                            icon.setOnAction(event -> viewModel.removeConnectivityDevice(devicePanelIconViewModel));
                            return icon;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();
    }

}
