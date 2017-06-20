package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelView extends VBox {

    private final DevicePanelViewModel viewModel;

    @FXML private VBox devicePanel;
    @FXML private FlowPane inputPane;
    @FXML private FlowPane outputPane;
    @FXML private FlowPane microcontrollerPane;
    @FXML public void onAddDeviceClick() {
        DeviceSelectorView deviceSelectorView = new DeviceSelectorView();
        Optional<ObservableList<ControlAddDevicePane>> result = deviceSelectorView.showAndWait();
        if (result.isPresent()) {
            viewModel.addDevice(result.get());
        }
    }

    private final ViewFactory<DevicePanelIconViewModel, DevicePanelIcon> viewFactory = new ViewFactory<DevicePanelIconViewModel, DevicePanelIcon>() {
        @Override
        public DevicePanelIcon newInstance(DevicePanelIconViewModel devicePanelIconViewModel) {
            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
            return icon;
        }
    };
    private final NodeConsumer<FlowPane, DevicePanelIcon> nodeConsumer = new NodeConsumer<FlowPane, DevicePanelIcon>() {
        @Override
        public void addNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().add(node);
        }

        @Override
        public void removeNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().remove(node);
        }
    };

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
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> inputViewCreator =
              new DynamicViewCreator<>(viewModel.getInputChildViewModel(), inputPane, viewFactory, nodeConsumer);
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
              new DynamicViewCreator<>(viewModel.getOutputChildViewModel(), outputPane, viewFactory, nodeConsumer);
    }
}
