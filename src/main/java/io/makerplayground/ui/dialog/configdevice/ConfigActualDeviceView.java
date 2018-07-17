package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.device.Property;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.ConnectionType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceView extends Dialog {
    private final ConfigActualDeviceViewModel viewModel;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox allDevice;
    @FXML private Label topicConfigDevice;
    @FXML private ImageView platFormImage;
    @FXML private Label platformName;
    @FXML private HBox platFormSelected;
    @FXML private HBox platFormPicture;
    @FXML private ComboBox<Platform> platFormComboBox;
    @FXML private VBox platFormAndController;
    @FXML private HBox entireControllerDevice;
    @FXML private ComboBox<Device> controllerComboBox;
    @FXML private Label controllerName;

    public ConfigActualDeviceView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/configdevice/ConfigActualDeviceView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.setOnCloseRequest(event -> stage.hide());

        initPlatformControl();
        initControllerControl();
        initDeviceControl();

        viewModel.setPlatformChangedCallback(this::initControllerControl);
        viewModel.setControllerChangedCallback(this::initDeviceControl);
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);

        platFormComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setPlatform(newValue));
        controllerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setController(newValue));
    }

    private void initPlatformControl() {
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

        platFormComboBox.getItems().clear();
        platFormComboBox.getItems().addAll(Platform.values());
        platFormComboBox.getSelectionModel().select(viewModel.getSelectedPlatform());
    }

    private void initControllerControl() {
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
        controllerComboBox.getItems().clear();
        controllerComboBox.getItems().addAll(viewModel.getCompatibleControllerDevice());
        if (viewModel.getSelectedController() != null) {
            controllerComboBox.getSelectionModel().select(viewModel.getSelectedController());
        }
    }

    private void initDeviceControl() {
        allDevice.getChildren().clear();
        DeviceMapper.DeviceMapperResult mappingResult = viewModel.getDeviceMapperResult();
        if (mappingResult == DeviceMapper.DeviceMapperResult.NO_MCU_SELECTED) {
            allDevice.getChildren().add(new Label("Controller hasn't been selected"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NOT_ENOUGH_PORT) {
            allDevice.getChildren().add(new Label("Controller doesn't have enough ports"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_SUPPORT_DEVICE) {
            allDevice.getChildren().add(new Label("Can't find any supported device"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.OK) {
            initDeviceControlChildren();
        } else {
            throw new IllegalStateException("Found unknown error!!!");
        }
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void initDeviceControlChildren() {
        viewModel.removeDeviceConfigChangedCallback();
        for (ProjectDevice projectDevice : viewModel.getAllDevice()) {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            imageView.setFitHeight(25.0);
            imageView.setFitWidth(25.0);

            Label name = new Label(projectDevice.getName());
            name.setTextAlignment(TextAlignment.LEFT);
            name.setAlignment(Pos.CENTER_LEFT);
            name.setId("nameLabel");

            HBox devicePic = new HBox();
            devicePic.setSpacing(10.0);
            devicePic.setAlignment(Pos.CENTER_LEFT);
            devicePic.setMaxHeight(25.0);
            devicePic.getChildren().addAll(imageView, name);

            // combobox of selectable devices
            ComboBox<Device> deviceComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleDevice(projectDevice)));
            deviceComboBox.setId("deviceComboBox");
            deviceComboBox.setCellFactory(new Callback<>() {
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
            deviceComboBox.setButtonCell(new ListCell<>() {
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
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // remove old selection if existed
                if (oldValue != null) {
                    viewModel.removePeripheral(projectDevice);
                }
                viewModel.setDevice(projectDevice, newValue);
            });
            if (projectDevice.getActualDevice() != null) {
                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }

            CheckBox checkBox = new CheckBox("Auto Select");
            checkBox.setSelected(projectDevice.isAutoSelectDevice());
            deviceComboBox.setDisable(projectDevice.isAutoSelectDevice());
            checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
                projectDevice.setAutoSelectDevice(new_val);
                deviceComboBox.setDisable(new_val);
            });

            VBox entireComboBoxDevice = new VBox();
            entireComboBoxDevice.setId("entireComboBoxDevice");
            entireComboBoxDevice.setPadding(new Insets(0,0,0,30));
            entireComboBoxDevice.getChildren().addAll(deviceComboBox);

            HBox portComboBoxHbox = new HBox();
            portComboBoxHbox.setSpacing(5.0);
            portComboBoxHbox.setAlignment(Pos.CENTER_LEFT);

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
                    portComboBox.setCellFactory(new Callback<>() {
                        @Override
                        public ListCell<List<DevicePort>> call(ListView<List<DevicePort>> param) {
                            return new ListCell<>() {
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
                    portComboBox.setButtonCell(new ListCell<>() {
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
                    if (!projectDevice.getDeviceConnection().isEmpty()) {
                        portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(p));
                    }
                    portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setPeripheral(projectDevice, p, newValue));
                    portComboBox.disableProperty().bind(checkBox.selectedProperty());

                    // TODO: handle other type (UART, SPI, etc.)
                    String portName;
                    if (p.getConnectionType() == ConnectionType.I2C) {
                        portName = "I2C";
                    } else {
                        portName = projectDevice.getActualDevice().getPort(p).get(0).getName();
                    }

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

            HBox entireDevice = new HBox();
            entireDevice.setSpacing(10.0);
            entireDevice.setAlignment(Pos.TOP_LEFT);
            entireDevice.getChildren().addAll(devicePic, checkBox, entireComboBoxDevice);

            allDevice.getChildren().add(entireDevice);
        }
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);
    }
}
