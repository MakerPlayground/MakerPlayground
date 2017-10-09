package io.makerplayground.ui.devicepanel;


import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.SingletonConfigDevice;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceView extends Dialog {
    private final ConfigActualDeviceViewModel viewModel;

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        getDialogPane().getStylesheets().add(this.getClass().getResource("/css/ConfigActualDeviceView.css").toExternalForm());

        setTitle("  Configure Device");
        getDialogPane().setExpanded(true);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);

        initView();
        viewModel.compatibleDeviceListProperty().addListener(observable -> initView());
        viewModel.compatiblePortListProperty().addListener(observable -> initView());
    }

    private void initView() {
        ScrollPane scrollPane = new ScrollPane();
        VBox allDevice = new VBox();

        scrollPane.setPrefHeight(260.0);
        scrollPane.setPrefWidth(565);
        allDevice.setMaxHeight(260.0);
        allDevice.setMaxWidth(Region.USE_COMPUTED_SIZE);
        allDevice.setSpacing(20.0);
        allDevice.setPadding(new Insets(20,30,20,30));
        allDevice.setAlignment(Pos.CENTER_LEFT);

        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            System.out.println("DID IT");
            System.out.println(viewModel.getAllDevice().size());
            for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
                if (projectDevice.isAutoSelectDevice())
                    SingletonConfigDevice.getInstance().setAll("123", "", projectDevice.isAutoSelectDevice(), "");
                else {
                    String port = String.join(",", projectDevice.getDeviceConnection().values().stream().flatMap(Collection::stream)
                            .map(DevicePort::getName).collect(Collectors.toList()));
                    SingletonConfigDevice.getInstance().setAll("123", projectDevice.getActualDevice().getId(), projectDevice.isAutoSelectDevice(), port);
                }
            }
            window.hide();
        });

        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            //VBox row = new VBox();
            //row.setAlignment(Pos.BASELINE_LEFT);
            HBox entireDevice = new HBox();
            //entireDevice.setAlignment(Pos.BASELINE_LEFT);
            HBox devicePic = new HBox();

            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            Label name = new Label(projectDevice.getName());
            devicePic.getChildren().addAll(imageView, name);

            devicePic.setSpacing(10.0);
            devicePic.setAlignment(Pos.CENTER);
            devicePic.setMinHeight(45.0);

            name.setTextAlignment(TextAlignment.LEFT);
            name.setAlignment(Pos.CENTER_LEFT);

            imageView.setFitHeight(25.0);
            imageView.setFitWidth(25.0);

            entireDevice.setSpacing(10.0);
            entireDevice.setAlignment(Pos.CENTER_LEFT);

            //row.setAlignment(Pos.CENTER);

            // combobox of selectable devices
            ComboBox<Device> deviceComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleDevice(projectDevice)));
            deviceComboBox.setId("deviceComboBox");
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

            if (projectDevice.getActualDevice() != null) {
                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setDevice(projectDevice, newValue);
                viewModel.removePeripheral(projectDevice);
                viewModel.reInitialize();
            });

            CheckBox checkBox = new CheckBox("Auto");
            checkBox.setSelected(projectDevice.isAutoSelectDevice());
            deviceComboBox.setDisable(projectDevice.isAutoSelectDevice());
            //portComboBox.setDisable(projectDevice.isAutoSelectDevice());
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    projectDevice.setAutoSelectDevice(new_val);
                    if (!new_val) {
                        deviceComboBox.setDisable(false);
                        //portComboBox.setDisable(false);
                    }
                    else {
                        deviceComboBox.setDisable(true);
                        //portComboBox.setDisable(true);
                    }
                }
            });

            entireDevice.getChildren().addAll(devicePic, checkBox, deviceComboBox);

            Map<Peripheral, List<List<DevicePort>>> combo = viewModel.getCompatiblePort(projectDevice);

            // We only show port combobox when the device has been selected
            Device actualDevice = projectDevice.getActualDevice();
            if (actualDevice != null) {
                // loop for each peripheral
                for (Peripheral p : /*combo.keySet()*/ actualDevice.getConnectivity()){
                    ComboBox<List<DevicePort>> portComboBox = new ComboBox<>(FXCollections.observableList(combo.get(p)));
                    portComboBox.setId("portComboBox");

                    portComboBox.setCellFactory(new Callback<ListView<List<DevicePort>>, ListCell<List<DevicePort>>>() {
                        @Override
                        public ListCell<List<DevicePort>> call(ListView<List<DevicePort>> param) {
                            return new ListCell<List<DevicePort>>() {
                                @Override
                                protected void updateItem(List<DevicePort> item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setText("");
                                    } else {
                                        setText(String.join(",", item.stream().map(DevicePort::getName).collect(Collectors.toList())));
                                    }
                                }
                            };
                        }
                    });
                    portComboBox.setButtonCell(new ListCell<List<DevicePort>>() {
                        @Override
                        protected void updateItem(List<DevicePort> item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText("");
                            } else {
                                setText(String.join(",", item.stream().map(DevicePort::getName).collect(Collectors.toList())));
                            }
                        }
                    });
                    if (projectDevice.getDeviceConnection().isEmpty()) {
                        //portComboBox.getSelectionModel().selectFirst();
                    } else {
                        portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(p));

                    }


                    if (!projectDevice.getDeviceConnection().isEmpty()) {
                        portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(p));
                    }
                    portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        viewModel.setPeripheral(projectDevice, p, newValue);
                    });

                    portComboBox.disableProperty().bind(checkBox.selectedProperty());

                    // TODO: check if we can safely get the first element
                    String portName = projectDevice.getActualDevice().getPort(p).get(0).getName();
                    entireDevice.getChildren().addAll(new Label(portName), portComboBox);
                }
            }

            allDevice.getChildren().add(entireDevice);
        }

        VBox vBox = new VBox();


        final Hyperlink hpl = new Hyperlink("Request your favorite devices");

        hpl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                SingletonUtilTools.getInstance().setAll("REQUEST");

                String s = "https://goo.gl/forms/12Wsu9WPZPumUPOj2";
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(URI.create(s));
                } catch (IOException ev) {
                    ev.printStackTrace();
                }
            }
        });


        vBox.getChildren().add(hpl);
        allDevice.getChildren().add(vBox);

        scrollPane.setContent(allDevice);


        getDialogPane().setContent(scrollPane);
    }
}
