package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.device.Property;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.*;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceView extends Dialog {
    private final ConfigActualDeviceViewModel viewModel;

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        getDialogPane().getStylesheets().add(this.getClass().getResource("/css/dialog/configdevice/ConfigActualDeviceView.css").toExternalForm());
        getDialogPane().setPrefHeight(500);
        getDialogPane().setMaxHeight(500);

        setTitle("Configure Device");
//        setResizable(true);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);

        initView();
        viewModel.setCallback(this::initView);
    }

    private void initView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox allDevice = new VBox();
        allDevice.setSpacing(20.0);
        allDevice.setPadding(new Insets(30,30,30,30));
        allDevice.setAlignment(Pos.CENTER);

        Label topicConfigDevice = new Label("Customize your Devices");
        topicConfigDevice.setId("topicConfigDevice");
        allDevice.getChildren().add(topicConfigDevice);

        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        initControllerControl(allDevice);

        DeviceMapper.DeviceMapperResult mappingResult = viewModel.getDeviceMapperResult();
        if (mappingResult == DeviceMapper.DeviceMapperResult.NO_MCU_SELECTED) {
            allDevice.getChildren().add(new Label("Controller hasn't been selected"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NOT_ENOUGH_PORT) {
            allDevice.getChildren().add(new Label("Controller doesn't have enough ports"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_SUPPORT_DEVICE) {
            allDevice.getChildren().add(new Label("Can't find any supported device"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.OK) {
            initDeviceControl(allDevice);
        } else {
            throw new IllegalStateException("Found unknown error!!!");
        }

        scrollPane.setContent(allDevice);
        getDialogPane().setContent(scrollPane);
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void initControllerControl(VBox allDevice) {
//        ImageView controllerImage = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/Controller.png")));
//        controllerImage.setFitHeight(25.0);
//        controllerImage.setFitWidth(25.0);

        ImageView platFormImage = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/Controller.png")));
        platFormImage.setFitHeight(25.0);
        platFormImage.setFitWidth(25.0);

        Label platformName = new Label("Platform");
        platformName.setTextAlignment(TextAlignment.LEFT);
        platformName.setAlignment(Pos.CENTER_LEFT);
        platformName.setId("platFormLabel");

        ComboBox<Platform> platFormComboBox = new ComboBox<>(FXCollections.observableArrayList(Platform.values()));
        platFormComboBox.setId("platformComboBox");
        platFormComboBox.getSelectionModel().select(viewModel.getSelectedPlatform());
        platFormComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Platform> call(ListView<Platform> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Platform item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getDisplayName());
                        }
                    }
                };
            }
        });
        platFormComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Platform item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        platFormComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setPlatform(newValue);
        });

        Label controllerName = new Label("Controller");
        controllerName.setTextAlignment(TextAlignment.LEFT);
        controllerName.setAlignment(Pos.CENTER_LEFT);
        controllerName.setId("nameLabel");

        ComboBox<Device> controllerComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleControllerDevice()));
        controllerComboBox.setId("controllerComboBox");
        controllerComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Device> call(ListView<Device> param) {
                return new ListCell<>() {
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
        controllerComboBox.setButtonCell(new ListCell<>() {
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
        if (viewModel.getSelectedController() != null) {
            controllerComboBox.getSelectionModel().select(viewModel.getSelectedController());
        }
        controllerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setController(newValue);
        });

        HBox platFormPicture = new HBox();
        platFormPicture.setSpacing(10.0);
        platFormPicture.setAlignment(Pos.CENTER_LEFT);
        platFormPicture.setMaxHeight(25.0);
        platFormPicture.getChildren().addAll(platFormImage, platformName);

        HBox platFormSelected = new HBox();
        platFormSelected.setSpacing(65.0);
        platFormSelected.setAlignment(Pos.TOP_LEFT);
        platFormSelected.getChildren().addAll(platFormPicture, platFormComboBox);

        HBox entireControllerDevice = new HBox();
        entireControllerDevice.setSpacing(12.0);
        entireControllerDevice.setAlignment(Pos.CENTER_LEFT);
        entireControllerDevice.getChildren().addAll(controllerName,controllerComboBox);
        entireControllerDevice.setId("controllerSelection");

        VBox platFormAndController = new VBox();
        platFormAndController.setSpacing(10);
        platFormAndController.getChildren().addAll(platFormSelected,entireControllerDevice);

        allDevice.getChildren().add(platFormAndController);
    }

    private void initDeviceControl(VBox allDevice) {
        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            //VBox row = new VBox();
            //row.setAlignment(Pos.BASELINE_LEFT);
            HBox entireDevice = new HBox();
            HBox portComboBoxHbox = new HBox();
            VBox entireComboBoxDevice = new VBox();
            entireComboBoxDevice.setId("entireComboBoxDevice");
            entireComboBoxDevice.setPadding(new Insets(0,0,0,30));
            //entireDevice.setAlignment(Pos.BASELINE_LEFT);
            HBox devicePic = new HBox();
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            Label name = new Label(projectDevice.getName());
            devicePic.getChildren().addAll(imageView, name);

            devicePic.setSpacing(10.0);
            devicePic.setAlignment(Pos.CENTER_LEFT);
            devicePic.setMaxHeight(25.0);

            name.setTextAlignment(TextAlignment.LEFT);
            name.setAlignment(Pos.CENTER_LEFT);
            name.setId("nameLabel");

            imageView.setFitHeight(25.0);
            imageView.setFitWidth(25.0);

            entireDevice.setSpacing(10.0);
            entireDevice.setAlignment(Pos.TOP_LEFT);

            portComboBoxHbox.setSpacing(5.0);

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
                // remove old selection if existed
                if (oldValue != null) {
                    viewModel.removePeripheral(projectDevice);
                }
                viewModel.setDevice(projectDevice, newValue);
            });

            CheckBox checkBox = new CheckBox("Auto Select");
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
            entireComboBoxDevice.getChildren().addAll(deviceComboBox);
            entireDevice.getChildren().addAll(devicePic, checkBox, entireComboBoxDevice);

            Map<Peripheral, List<List<DevicePort>>> combo = viewModel.getCompatiblePort(projectDevice);

            // We only show port combobox when the device has been selected
            Device actualDevice = projectDevice.getActualDevice();
            if (actualDevice != null) {
                // loop for each peripheral
                for (Peripheral p : /*combo.keySet()*/ actualDevice.getConnectivity()){
                    if (combo.get(p) == null)
                        continue;

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

                    // TODO: handle other type (UART, SPI, etc.)
                    String portName;
                    if (p.getConnectionType() == ConnectionType.I2C) {
                        portName = "I2C";
                    } else {
                        portName = projectDevice.getActualDevice().getPort(p).get(0).getName();
                    }
                    portComboBoxHbox.setAlignment(Pos.CENTER_LEFT);
                    portComboBoxHbox.getChildren().addAll(new Label(portName), portComboBox);
                }
            }
            entireComboBoxDevice.getChildren().add(portComboBoxHbox);
            entireComboBoxDevice.setSpacing(10.0);

            // property
            if (!projectDevice.getGenericDevice().getProperty().isEmpty()) {
                GridPane propertyGridPane = new GridPane();
                propertyGridPane.setHgap(10);
                propertyGridPane.setVgap(10);

                List<Property> propertyList = projectDevice.getGenericDevice().getProperty();
                for (int i=0; i<propertyList.size(); i++) {
                    Property p = propertyList.get(i);

                    Label propertyLabel = new Label(p.getName());
                    GridPane.setRowIndex(propertyLabel, i);
                    GridPane.setColumnIndex(propertyLabel, 0);
                    propertyGridPane.getChildren().add(propertyLabel);

                    if (p.getType() == DataType.STRING) {
                        TextField textField = new TextField(projectDevice.getPropertyValue(p));
                        textField.textProperty().addListener((observable, oldValue, newValue) -> projectDevice.setPropertyValue(p, newValue));
                        GridPane.setRowIndex(textField, i);
                        GridPane.setColumnIndex(textField, 1);
                        propertyGridPane.getChildren().add(textField);
                    } else {    // TODO: add support for new property type
                        throw new IllegalStateException("Found unknown property type");
                    }
                }

                entireComboBoxDevice.getChildren().add(propertyGridPane);
            }

            allDevice.getChildren().add(entireDevice);
        }
    }
}
