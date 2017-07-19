package io.makerplayground.ui.devicepanel;


import io.makerplayground.device.Device;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceView extends Dialog {
    private final ConfigActualDeviceViewModel viewModel;
    private final Map<ProjectDevice, List<Peripheral>> compatiblePort;

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        this.compatiblePort = viewModel.getDeviceCompatiblePort();
        initView();
    }

    private void initView() {
        Dialog dialog = new Dialog();
        ScrollPane scrollPane = new ScrollPane();
        VBox allDevice = new VBox();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            VBox row = new VBox();
            HBox entireDevice = new HBox();
            VBox devicePic = new VBox();
            Image image = new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                                    + projectDevice.getGenericDevice().getName() + ".png"));
            ImageView imageView = new ImageView(image);
            Label name = new Label();
            name.setText(projectDevice.getName());
            devicePic.getChildren().addAll(imageView, name);

            // combobox of selectable devices
            ObservableList<Device> oDeviceList = FXCollections.observableArrayList(viewModel.getCompatibleDevice(projectDevice));
            ComboBox<Device> deviceComboBox = new ComboBox<>(oDeviceList);
            deviceComboBox.setCellFactory(new Callback<ListView<Device>, ListCell<Device>>() {
                @Override
                public ListCell<Device> call(ListView<Device> param) {
                    return new ListCell<Device>() {
                        @Override
                        protected void updateItem(Device item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText("");
                            } else {
                                setText(item.getBrand() + " " + item.getModel());
                            }
                        }
                    };
                }
            });
            deviceComboBox.setButtonCell(new ListCell<Device>(){
                @Override
                protected void updateItem(Device item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getBrand() + " " + item.getModel());
                    }
                }
            });
            if (projectDevice.getActualDevice() == null) {
                deviceComboBox.getSelectionModel().selectFirst();
            } else {
                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setDevice(projectDevice, newValue);
            });

            CheckBox checkBox = new CheckBox("Auto");
            if (projectDevice.isAutoSelectDevice()) {
                checkBox.setSelected(true);
                deviceComboBox.setDisable(true);
            }
            else {
                checkBox.setSelected(false);
                deviceComboBox.setDisable(false);
            }
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov,
                                    Boolean old_val, Boolean new_val) {
                    projectDevice.setAutoSelectDevice(new_val);
                    if (new_val == true) {
                        deviceComboBox.setDisable(true);
                        viewModel.setDevice(projectDevice, deviceComboBox.getValue());
                        System.out.println("new value is " + deviceComboBox.getValue());
                    }
                    else {
                        deviceComboBox.setDisable(false);
                        viewModel.setDevice(projectDevice, null);
                    }
                }
            });

            // combobox of selectable port
            ObservableList<Peripheral> oPortList = FXCollections.observableArrayList(compatiblePort.get(projectDevice));
            ComboBox<Peripheral> portComboBox = new ComboBox<>(oPortList);
            portComboBox.setCellFactory(new Callback<ListView<Peripheral>, ListCell<Peripheral>>() {
                @Override
                public ListCell<Peripheral> call(ListView<Peripheral> param) {
                    return new ListCell<Peripheral>() {
                        @Override
                        protected void updateItem(Peripheral item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText("");
                            } else {
                                setText(item.toString());
                            }
                        }
                    };
                }
            });
            portComboBox.setButtonCell(new ListCell<Peripheral>(){
                @Override
                protected void updateItem(Peripheral item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            });
            portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                Map<Peripheral, Peripheral> result = new HashMap<>();
                result.put(projectDevice.getActualDevice().getConnectivity().get(0), newValue);
                projectDevice.setDeviceConnection(result);
            });

            entireDevice.getChildren().addAll(devicePic, checkBox, deviceComboBox, portComboBox);
            row.getChildren().add(entireDevice);
            allDevice.getChildren().add(row);
        }

        scrollPane.setContent(allDevice);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
}
