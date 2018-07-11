package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.deprecated.DevicePanelIconViewModel;
import io.makerplayground.ui.dialog.devicepane.DeviceSelectorView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
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
    private DevicePanelIconViewModel devicePanelIconViewModel;

    @FXML
    private VBox devicePanel;
    @FXML
    private ListView<ProjectDevice> sensorPane;
    @FXML
    private ListView<ProjectDevice> actuatorPane;
    @FXML
    private ListView<ProjectDevice> connectivityPane;

    @FXML
    public void onAddDeviceClick() {
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
        sensorPane.setEditable(true);
        actuatorPane.setEditable(true);
        connectivityPane.setEditable(true);
        sensorPane.setCellFactory(param -> new DevicePanelListCell(viewModel.getProject()));
        sensorPane.setItems(viewModel.getSensor());
        actuatorPane.setCellFactory(param -> new DevicePanelListCell(viewModel.getProject()));
        actuatorPane.setItems(viewModel.getActuator());
        connectivityPane.setCellFactory(param -> new DevicePanelListCell(viewModel.getProject()));
        connectivityPane.setItems(viewModel.getConnectivity());
    }
}