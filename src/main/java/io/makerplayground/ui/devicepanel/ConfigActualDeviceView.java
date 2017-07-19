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

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
        viewModel.compatibleDeviceListProperty().addListener(observable -> initView());
        viewModel.compatiblePortListProperty().addListener(observable -> initView());
    }

    private void initView() {
        ScrollPane scrollPane = new ScrollPane();
        VBox allDevice = new VBox();
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            VBox row = new VBox();
            HBox entireDevice = new HBox();
            VBox devicePic = new VBox();

            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            Label name = new Label(projectDevice.getName());
            devicePic.getChildren().addAll(imageView, name);

            // combobox of selectable port
            ComboBox<Peripheral> portComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatiblePort(projectDevice)));
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
//            if (projectDevice.getDeviceConnection().isEmpty()) {
//                portComboBox.getSelectionModel().selectFirst();
//            } else {
//                portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(
//                        projectDevice.getActualDevice().getConnectivity().get(0)));
//
//            }
            if (!projectDevice.getDeviceConnection().isEmpty()) {
                portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(
                        projectDevice.getActualDevice().getConnectivity().get(0)));
            }
            portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setPeripheral(projectDevice, newValue);
            });

            // combobox of selectable devices
            ComboBox<Device> deviceComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleDevice(projectDevice)));
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
            deviceComboBox.setButtonCell(new ListCell<Device>() {
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
//            if (projectDevice.getActualDevice() == null) {
//                deviceComboBox.getSelectionModel().selectFirst();
//            } else {
//                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
//            }
            if (projectDevice.getActualDevice() != null) {
                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setDevice(projectDevice, newValue);
                viewModel.removePeripheral(projectDevice);
            });

            CheckBox checkBox = new CheckBox("Auto");
            checkBox.setSelected(projectDevice.isAutoSelectDevice());
            deviceComboBox.setDisable(projectDevice.isAutoSelectDevice());
            portComboBox.setDisable(projectDevice.isAutoSelectDevice());
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    projectDevice.setAutoSelectDevice(new_val);
                    if (!new_val) {
                        deviceComboBox.setDisable(false);
                        portComboBox.setDisable(false);
                        //viewModel.setDevice(projectDevice, deviceComboBox.getValue());
                        //viewModel.setPeripheral(projectDevice, portComboBox.getValue());
                        //System.out.println("new value is " + deviceComboBox.getValue() + " " + portComboBox.getValue());
                    }
                    else {
                        deviceComboBox.setDisable(true);
                        portComboBox.setDisable(true);
                        //viewModel.setDevice(projectDevice, null);
                    }
                }
            });

            entireDevice.getChildren().addAll(devicePic, checkBox, deviceComboBox, portComboBox);
            row.getChildren().add(entireDevice);
            allDevice.getChildren().add(row);
        }

        scrollPane.setContent(allDevice);
        getDialogPane().setContent(scrollPane);
    }
}
