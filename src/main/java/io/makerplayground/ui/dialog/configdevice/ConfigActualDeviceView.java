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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigActualDeviceView extends Stage {

    private final Window owner;
    private final ConfigActualDeviceViewModel viewModel;

    private final ScrollPane scrollPane = new ScrollPane();
    @FXML private VBox usedDevice;
    @FXML private FlowPane unusedDevicePane;
    @FXML private VBox unusedDevice;
    @FXML private ImageView platFormImage;
    @FXML private Label platformName;
    @FXML private ComboBox<Platform> platFormComboBox;
    @FXML private ComboBox<Device> controllerComboBox;
    @FXML private Label controllerName;
    @FXML private Button okButton;

    public ConfigActualDeviceView(Window owner, ConfigActualDeviceViewModel viewModel) {
        this.owner = owner;
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/configdevice/ConfigActualDeviceView.fxml"));
        fxmlLoader.setRoot(scrollPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        initPlatformControl();
        initControllerControl();
        initDeviceControl();

        initOwner(owner);
        initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(scrollPane);
        setScene(scene);

        initEvent();
    }

    private void initEvent() {
        // redraw when needed
        viewModel.setPlatformChangedCallback(this::initControllerControl);
        viewModel.setControllerChangedCallback(this::initDeviceControl);
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);

        // write change to the viewmodel
        platFormComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setPlatform(newValue));
        controllerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setController(newValue));

        // allow the dialog to be closed
        okButton.setOnAction(event -> hide());

        addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                hide();
            }
        });

        // JavaFX's modal stage blocks event to other stage so we can't allow user to close this dialog by pressing at
        // the surround space. Thus, we consume every mouse event to the parent window here to simulate behaviour of a
        // modal dialog and close ourselves when detect MOUSE_PRESSED at the parent window
        Parent rootPane = owner.getScene().getRoot();
        rootPane.addEventFilter(MouseEvent.ANY, new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    rootPane.removeEventFilter(MouseEvent.ANY, this);
                    hide();
                }
                event.consume();
            }
        });

        // dim the parent window after the dialog is shown on the screen
        Effect previousEffect = rootPane.getEffect();

        ColorAdjust colorAdjust = new ColorAdjust(0, 0, 0, 0);
        rootPane.setEffect(colorAdjust);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(750),
                new KeyValue(colorAdjust.brightnessProperty(), -0.25)
        ));
        setOnShowing(event -> timeline.play());
        setOnHidden(t -> rootPane.setEffect(previousEffect));
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
        usedDevice.getChildren().clear();
        unusedDevicePane.getChildren().clear();
        DeviceMapper.DeviceMapperResult mappingResult = viewModel.getDeviceMapperResult();
        if (mappingResult == DeviceMapper.DeviceMapperResult.NO_MCU_SELECTED) {
            usedDevice.getChildren().add(new Label("Controller hasn't been selected"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NOT_ENOUGH_PORT) {
            usedDevice.getChildren().add(new Label("Controller doesn't have enough ports"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_SUPPORT_DEVICE) {
            usedDevice.getChildren().add(new Label("Can't find any supported device"));
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.OK){
            initDeviceControlChildren();
            initUnusedDeviceControl();
        } else {
            throw new IllegalStateException("Found unknown error!!!");
        }
        // resize this stage according to the underlying scene so that the window's size change based on the content of the dialog
        sizeToScene();
    }

    private void initDeviceControlChildren() {
        viewModel.removeDeviceConfigChangedCallback();
        for (ProjectDevice projectDevice : viewModel.getUsedDevice()) {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            imageView.setFitHeight(30.0);
            imageView.setFitWidth(30.0);

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
            if (projectDevice.getActualDevice() != null) {
                deviceComboBox.getSelectionModel().select(projectDevice.getActualDevice());
            }
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setDevice(projectDevice, newValue);
            });

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
                    } else {
                        portComboBox.getSelectionModel().selectFirst();
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

            usedDevice.getChildren().add(entireDevice);
        }
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);
    }

    private void initUnusedDeviceControl() {
        if (viewModel.getUnusedDevice().isEmpty()) {
            unusedDevice.setVisible(false);
            unusedDevice.setManaged(false);
        } else {
            unusedDevice.setVisible(true);
            unusedDevice.setManaged(true);
            for (ProjectDevice projectDevice : viewModel.getUnusedDevice()) {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/"
                        + projectDevice.getGenericDevice().getName() + ".png")));
                imageView.setFitHeight(30.0);
                imageView.setFitWidth(30.0);

                Label name = new Label(projectDevice.getName());
                name.setTextAlignment(TextAlignment.LEFT);
                name.setAlignment(Pos.CENTER_LEFT);
                name.setId("nameLabel");

                HBox devicePic = new HBox();
                devicePic.setSpacing(10.0);
                devicePic.setAlignment(Pos.CENTER_LEFT);
                devicePic.setMaxHeight(25.0);
                devicePic.getChildren().addAll(imageView, name);

                HBox entireDevice = new HBox();
                entireDevice.setSpacing(10.0);
                entireDevice.setAlignment(Pos.TOP_LEFT);
                entireDevice.getChildren().addAll(devicePic);

                unusedDevicePane.getChildren().add(entireDevice);
            }
        }
    }
}
