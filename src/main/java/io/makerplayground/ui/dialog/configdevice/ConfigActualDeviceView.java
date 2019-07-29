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

package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.project.PinPortConnection;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SpinnerWithUnit;
import io.makerplayground.ui.control.AzurePropertyControl;
import io.makerplayground.ui.dialog.AzureSettingDialog;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigActualDeviceView extends VBox{

    private final ConfigActualDeviceViewModel viewModel;

    @FXML private VBox usedDevice;
    @FXML private GridPane usedDeviceSettingPane;
    @FXML private Button autoButton;
    @FXML private VBox unusedDevice;
    @FXML private FlowPane unusedDevicePane;
    @FXML private VBox cloudPlatformParameterSection;
    @FXML private GridPane cloudPlatformParameterPane;
    @FXML private ImageView platFormImage;
    @FXML private Label platformName;
    @FXML private ComboBox<Platform> platFormComboBox;
    @FXML private ComboBox<ActualDeviceComboItem> controllerComboBox;
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

        initPlatformControl();
        initControllerControl();
        initDeviceControl();
        initEvent();
    }

    private void initEvent() {
        // redraw when needed
        viewModel.setPlatformChangedCallback(this::initControllerControl);
        viewModel.setControllerChangedCallback(this::initDeviceControl);
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);

        // write change to the viewModel
        platFormComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setPlatform(newValue));
        controllerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setController(newValue));

        autoButton.setOnAction(event -> {
            /* TODO: uncomment this */
//            ProjectMappingResult result = viewModel.autoAssignDevice();
//            if (result != ProjectMappingResult.OK) {
//                WarningDialogView warningDialogView = new WarningDialogView(getScene().getWindow(), result.getErrorMessage());
//                warningDialogView.showAndWait();
//            }
        });
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
            public ListCell<ActualDeviceComboItem> call(ListView<ActualDeviceComboItem> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ActualDeviceComboItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getActualDevice().getBrand() + " " + item.getActualDevice().getModel());
                            setBackground(item.getMappingResult() == DeviceMappingResult.OK ? Background.EMPTY : new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                        }
                    }
                };
            }
        });
        controllerComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ActualDeviceComboItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getActualDevice().getBrand() + " " + item.getActualDevice().getModel());
                    setBackground(item.getMappingResult() == DeviceMappingResult.OK ? Background.EMPTY : new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });
        List<ActualDeviceComboItem> controllerList = viewModel.getControllerComboItemList(viewModel.getSelectedPlatform());
        controllerComboBox.getItems().clear();
        controllerComboBox.getItems().addAll(controllerList);
        controllerComboBox.getItems().stream()
                .filter(actualDeviceComboItem -> actualDeviceComboItem.getActualDevice() == viewModel.getSelectedController())
                .findFirst()
                .ifPresentOrElse(selectedController -> controllerComboBox.getSelectionModel().select(selectedController), this::initDeviceControl);
    }

    private void initDeviceControl() {
        usedDevice.setVisible(false);
        usedDevice.setManaged(false);
        unusedDevice.setVisible(false);
        unusedDevice.setManaged(false);
        cloudPlatformParameterSection.setVisible(false);
        cloudPlatformParameterSection.setManaged(false);
        initDeviceControlChildren();
        initUnusedDeviceControl();
        initCloudPlatformPropertyControl();
    }

    private Callback<ListView<CompatibleDeviceComboItem>, ListCell<CompatibleDeviceComboItem>> newDeviceCellFactory() {
        return new Callback<>() {
            @Override
            public ListCell<CompatibleDeviceComboItem> call(ListView<CompatibleDeviceComboItem> param) {
                final Background GREY_BG = new Background(new BackgroundFill(Color.gray(0.8), CornerRadii.EMPTY, Insets.EMPTY));
                ListCell<CompatibleDeviceComboItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(CompatibleDeviceComboItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            if (item.getCompatibleDevice().getActualDevice().isPresent()) {
                                ActualDevice actualDevice = item.getCompatibleDevice().getActualDevice().get();
                                setText(actualDevice.getBrand() + " " + actualDevice.getModel());
                            } else if (item.getCompatibleDevice().getProjectDevice().isPresent()) {
                                ProjectDevice projectDevice = item.getCompatibleDevice().getProjectDevice().get();
                                setText("Same as " + projectDevice.getName());
                            }
                            setBackground(item.getDeviceMappingResult() == DeviceMappingResult.OK ? Background.EMPTY : GREY_BG);
                            if (item.getDeviceMappingResult() != DeviceMappingResult.OK) {
                                Tooltip tooltip = new Tooltip("Device not supported (reason: " + item.getDeviceMappingResult().getErrorMessage() + ")");
                                setTooltip(tooltip);
                            }
                        }
                    }
                };
                cell.hoverProperty().addListener((observable, wasHover, isNowHover) -> {
                    if (!cell.isEmpty()) {
                        if (cell.getItem().getDeviceMappingResult() != DeviceMappingResult.OK) {
                            cell.setBackground(GREY_BG);
                        }
                        else if (!isNowHover) {
                            cell.setBackground(Background.EMPTY);
                        }
                    }
                });
                return cell;
            }
        };
    }

    private ListCell<CompatibleDeviceComboItem> newDeviceComboItemListCell() {
        return new ListCell<>() {
            final Background GREY_BG = new Background(new BackgroundFill(Color.gray(0.8), CornerRadii.EMPTY, Insets.EMPTY));
            @Override
            protected void updateItem(CompatibleDeviceComboItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setTooltip(null);
                } else {
                    if (item.getCompatibleDevice().getActualDevice().isPresent()) {
                        ActualDevice actualDevice = item.getCompatibleDevice().getActualDevice().get();
                        setText(actualDevice.getBrand() + " " + actualDevice.getModel());
                    } else if (item.getCompatibleDevice().getProjectDevice().isPresent()) {
                        ProjectDevice projectDevice = item.getCompatibleDevice().getProjectDevice().get();
                        setText("Same as " + projectDevice.getName());
                    }
                    setBackground(item.getDeviceMappingResult() == DeviceMappingResult.OK ? Background.EMPTY : GREY_BG);
                    if (item.getDeviceMappingResult() != DeviceMappingResult.OK) {
                        Tooltip tooltip = new Tooltip("Device not supported (reason: " + item.getDeviceMappingResult().getErrorMessage() + ")");
                        setTooltip(tooltip);
                    }
                }
            }
        };
    }

    private String getPinPortConnectionString(PinPortConnection connection) {
        String text = "";

        if (!connection.getPinMapConsumerProvider().isEmpty()) {
            text += connection.getPinMapConsumerProvider().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> e.getValue().getDisplayName()).collect(Collectors.joining("-"));
        }
        if (!connection.getPinMapConsumerProvider().isEmpty() && !connection.getPortMapConsumerProvider().isEmpty()) {
            text += " / ";
        }
        if (!connection.getPortMapConsumerProvider().isEmpty()) {
            text += connection.getPortMapConsumerProvider().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> e.getValue().getName()).collect(Collectors.joining("-"));
        }

        return text;
    }

    private ListCell<PinPortConnection> newPinPortConnectionListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(PinPortConnection item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item != PinPortConnection.NOT_CONNECTED ? getPinPortConnectionString(item) : null);
                }
            }
        };
    }

    private Callback<ListView<PinPortConnection>, ListCell<PinPortConnection>> newPinPortConnectionCellFactory() {
        return new Callback<>() {
            @Override
            public ListCell<PinPortConnection> call(ListView<PinPortConnection> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(PinPortConnection item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item != PinPortConnection.NOT_CONNECTED ? getPinPortConnectionString(item) : null);
                        }
                    }
                };
            }
        };
    }

    private void initDeviceControlChildren() {
        if (viewModel.getUsedDevice().isEmpty()) {
            usedDevice.setVisible(false);
            usedDevice.setManaged(false);
        } else {
            usedDevice.setVisible(true);
            usedDevice.setManaged(true);
        }

        usedDeviceSettingPane.getChildren().clear();
        viewModel.clearDeviceConfigChangedCallback();
        int currentRow = 0;
        for (ProjectDevice projectDevice : viewModel.getUsedDevice()) {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            imageView.setFitHeight(30.0);
            imageView.setFitWidth(30.0);
            GridPane.setConstraints(imageView, 0, currentRow, 1, 1, HPos.LEFT, VPos.TOP);

            Label name = new Label(projectDevice.getName());
            name.setMinHeight(25); // a hack to center the label to the height of 1 row control when the control spans to multiple rows
            name.setTextAlignment(TextAlignment.LEFT);
            name.setAlignment(Pos.CENTER_LEFT);
            name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            name.setId("nameLabel");
            GridPane.setConstraints(name, 1, currentRow, 1, 1, HPos.LEFT, VPos.TOP);

            // combobox of selectable devices
            ComboBox<CompatibleDeviceComboItem> deviceComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleDeviceComboItem(projectDevice)));
            deviceComboBox.setId("deviceComboBox");
            deviceComboBox.setCellFactory(newDeviceCellFactory());
            deviceComboBox.setButtonCell(newDeviceComboItemListCell());
            deviceComboBox.getItems().stream()
                    .filter(compatibleDeviceComboItem -> {
                        Optional<ActualDevice> actualDevice = compatibleDeviceComboItem.getCompatibleDevice().getActualDevice();
                        return actualDevice.isPresent() && actualDevice.get() == viewModel.getActualDevice(projectDevice).orElse(null);
                    })
                    .findFirst()
                    .ifPresent(compatibleDeviceComboItem -> deviceComboBox.getSelectionModel().select(compatibleDeviceComboItem));
            deviceComboBox.getItems().stream()
                    .filter(compatibleDeviceComboItem -> {
                        Optional<ProjectDevice> projectDevice1 = compatibleDeviceComboItem.getCompatibleDevice().getProjectDevice();
                        return projectDevice1.isPresent() && projectDevice1.get() == viewModel.getParentDevice(projectDevice).orElse(null);
                    })
                    .findFirst()
                    .ifPresent(compatibleDeviceComboItem -> deviceComboBox.getSelectionModel().select(compatibleDeviceComboItem));
            deviceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setDevice(projectDevice, newValue.getCompatibleDevice()));

            VBox entireComboBoxDevice = new VBox();
            entireComboBoxDevice.setSpacing(10.0);
            entireComboBoxDevice.setId("entireComboBoxDevice");
            entireComboBoxDevice.getChildren().addAll(deviceComboBox);
            GridPane.setConstraints(entireComboBoxDevice, 2, currentRow, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.SOMETIMES);

            FlowPane portPane = new FlowPane();
            portPane.setHgap(5.0);
            portPane.setVgap(5.0);
            portPane.setAlignment(Pos.CENTER_LEFT);

            if (deviceComboBox.getSelectionModel().getSelectedItem() != null) {
                CompatibleDevice compatibleDevice = deviceComboBox.getSelectionModel().getSelectedItem().getCompatibleDevice();
                if (deviceComboBox.getSelectionModel().getSelectedItem().getDeviceMappingResult() == DeviceMappingResult.OK) {
                    compatibleDevice.getActualDevice().ifPresent(actualDevice -> {
                        ComboBox<PinPortConnection> portComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getPossiblePinPortConnections(projectDevice, actualDevice)));
                        portComboBox.setCellFactory(newPinPortConnectionCellFactory());
                        portComboBox.setButtonCell(newPinPortConnectionListCell());
                        PinPortConnection connection = viewModel.getSelectedPinPortConnection(projectDevice);
                        if (connection != PinPortConnection.NOT_CONNECTED) {
                            portComboBox.getSelectionModel().select(connection);
                        }
                        portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setDevicePinPortConnection(projectDevice, newValue));

                        List<Pin> pinConsume = actualDevice.getPinConsumeByOwnerDevice(null);
                        List<Port> portConsume = actualDevice.getPortConsumeByOwnerDevice(null);
                        String label = pinConsume.stream().sorted().map(Pin::getDisplayName).collect(Collectors.joining("-"));
                        if (!pinConsume.isEmpty() && !portConsume.isEmpty()) {
                            label += " / ";
                        }
                        label += portConsume.stream().sorted().map(Port::getName).collect(Collectors.joining("-"));

                        Tooltip tooltip = new Tooltip(label);
                        tooltip.setShowDelay(Duration.millis(50));
                        Label portLabel = new Label("Port");
                        portLabel.setTooltip(tooltip);
                        HBox portHBox = new HBox();
                        portHBox.setAlignment(Pos.CENTER_LEFT);
                        portHBox.getChildren().addAll(portLabel, portComboBox);
                        portHBox.setSpacing(5);
                        entireComboBoxDevice.getChildren().add(portHBox);
                    });
                }
            }

            // We only show port combobox and property textfield when the device has been selected
            if (viewModel.isActualDevicePresent(projectDevice)) {
//                ActualDevice actualDevice = viewModel.getActualDevice(projectDevice).orElseThrow();

                /* TODO: uncomment this */
//                if (!actualDevice.getPort(Peripheral.NOT_CONNECTED).isEmpty()) {
//                    viewModel.setPeripheral(projectDevice, Peripheral.NOT_CONNECTED, Collections.emptyList());
//                } else {
//                    // loop for each peripheral
//                    for (Peripheral p : /*combo.keySet()*/ actualDevice.getConnectivity()){
//                        if (combo.get(p) == null)
//                            continue;
//
//                        ComboBox<List<DevicePort>> portComboBox = new ComboBox<>(FXCollections.observableList(combo.get(p)));
//                        portComboBox.setId("portComboBox");
//                        portComboBox.setCellFactory(new Callback<>() {
//                            @Override
//                            public ListCell<List<DevicePort>> call(ListView<List<DevicePort>> param) {
//                                return new ListCell<>() {
//                                    @Override
//                                    protected void updateItem(List<DevicePort> item, boolean empty) {
//                                        super.updateItem(item, empty);
//                                        if (empty) {
//                                            setText("");
//                                        } else {
//                                            setText(String.join(",", item.stream().map(DevicePort::getDisplayName).collect(Collectors.toList())));
//                                        }
//                                    }
//                                };
//                            }
//                        });
//                        portComboBox.setButtonCell(new ListCell<>() {
//                            @Override
//                            protected void updateItem(List<DevicePort> item, boolean empty) {
//                                super.updateItem(item, empty);
//                                if (empty) {
//                                    setText("");
//                                } else {
//                                    setText(String.join(",", item.stream().map(DevicePort::getDisplayName).collect(Collectors.toList())));
//                                }
//                            }
//                        });

//                        if (!projectDevice.getDeviceConnection().isEmpty()) {
//                            // add dummy value to position 1 (after the selected item) to allow the selected port to be cleared
//                            portComboBox.getItems().add(1, Collections.emptyList());
//                            portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(p));
//                        } else {
//                            // add dummy value to allow the selected port to be cleared
//                            portComboBox.getItems().add(0, Collections.emptyList());
//                            portComboBox.getSelectionModel().select(Collections.emptyList());
//                        }
//                        portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                            if (newValue.isEmpty()) {
//                                viewModel.clearPeripheral(projectDevice, p);
//                            } else {
//                                viewModel.setPeripheral(projectDevice, p, newValue);
//                            }
//                        });
//
//                        // TODO: handle other type (UART, SPI, etc.)
//                        String portName;
//                        if (p.getConnectionType() == ConnectionType.I2C) {
//                            portName = "I2C";
//                        } else if (p.getConnectionType() == ConnectionType.UART) {
//                            portName = "UART";
//                        } else {
//                            portName = projectDevice.getActualDevice().getPort(p).get(0).getDisplayName();
//                        }
//
//                        Label portLabel = new Label(portName);
//
//                        HBox portHBox = new HBox();
//                        portHBox.getChildren().addAll(portLabel, portComboBox);
//                        portHBox.setSpacing(5);
//
//                        portPane.getChildren().addAll(portHBox);
//                    }
//                    entireComboBoxDevice.getChildren().add(portPane);
//                }

                // property
                if (viewModel.getActualDevice(projectDevice).orElseThrow().getProperty() != null && !viewModel.getActualDevice(projectDevice).orElseThrow().getProperty().isEmpty()) {
                    GridPane propertyGridPane = new GridPane();
                    propertyGridPane.setHgap(10);
                    propertyGridPane.setVgap(10);

                    List<Property> propertyList = viewModel.getActualDevice(projectDevice).orElseThrow().getProperty();
                    for (int i=0; i<propertyList.size(); i++) {
                        Property p = propertyList.get(i);

                        Label propertyLabel = new Label(p.getName());
                        GridPane.setRowIndex(propertyLabel, i);
                        GridPane.setColumnIndex(propertyLabel, 0);
                        propertyGridPane.getChildren().add(propertyLabel);

                        Object currentValue = viewModel.getPropertyValue(projectDevice, p);
                        if (p.getDataType() == DataType.STRING && p.getControlType() == ControlType.TEXTBOX) {
                            TextField textField = new TextField((String) currentValue);
                            textField.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(textField, i);
                            GridPane.setColumnIndex(textField, 1);
                            propertyGridPane.getChildren().add(textField);
                        } else if (p.getDataType() == DataType.ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            comboBox.getSelectionModel().select((String) currentValue);
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            propertyGridPane.getChildren().add(comboBox);
                        } else if (p.getDataType() == DataType.INTEGER_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            // TODO: we should create a variant of CategoricalConstraint that support list of other type instead of String
                            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            comboBox.getSelectionModel().select(String.valueOf(currentValue));
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, Integer.parseInt(newValue)));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            propertyGridPane.getChildren().add(comboBox);
                        } else if (p.getDataType() == DataType.BOOLEAN_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            comboBox.getSelectionModel().select(String.valueOf(currentValue));
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, Boolean.parseBoolean(newValue)));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            propertyGridPane.getChildren().add(comboBox);
                        } else if ((p.getDataType() == DataType.INTEGER || p.getDataType() == DataType.DOUBLE)
                                && p.getControlType() == ControlType.SPINBOX) {
                            SpinnerWithUnit spinner = new SpinnerWithUnit(p.getMinimumValue(), p.getMaximumValue()
                                    , List.of(p.getUnit()), (NumberWithUnit) currentValue);
                            spinner.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(spinner, i);
                            GridPane.setColumnIndex(spinner, 1);
                            propertyGridPane.getChildren().add(spinner);
                        } else if (p.getDataType() == DataType.AZURE_COGNITIVE_KEY && p.getControlType() == ControlType.AZURE_WIZARD) {
                            AzurePropertyControl<AzureCognitiveServices> control = new AzurePropertyControl<>(AzureSettingDialog.Service.COGNITIVE_SERVICE
                                    , (AzureCognitiveServices) currentValue);
                            control.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(control, i);
                            GridPane.setColumnIndex(control, 1);
                            propertyGridPane.getChildren().add(control);
                        } else if (p.getDataType() == DataType.AZURE_IOTHUB_KEY && p.getControlType() == ControlType.AZURE_WIZARD) {
                            AzurePropertyControl<AzureIoTHubDevice> control = new AzurePropertyControl<>(AzureSettingDialog.Service.IOT_HUB
                                    , (AzureIoTHubDevice) currentValue);
                            control.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(control, i);
                            GridPane.setColumnIndex(control, 1);
                            propertyGridPane.getChildren().add(control);
                        } else {    // TODO: add support for new property type
                            throw new IllegalStateException("Found unknown property type");
                        }
                    }
                    entireComboBoxDevice.getChildren().add(propertyGridPane);
                }
            }

            usedDeviceSettingPane.getChildren().addAll(imageView, name, entireComboBoxDevice);
            currentRow++;
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
            unusedDevicePane.getChildren().clear();
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

    private void initCloudPlatformPropertyControl() {
        if (viewModel.getCloudPlatformUsed().isEmpty()) {
            cloudPlatformParameterSection.setVisible(false);
            cloudPlatformParameterSection.setManaged(false);
        } else {
            cloudPlatformParameterSection.setVisible(true);
            cloudPlatformParameterSection.setManaged(true);

            int currentRow = 0;
            cloudPlatformParameterPane.getChildren().clear();
            for (CloudPlatform cloudPlatform : viewModel.getCloudPlatformUsed()) {
                ImageView cloudPlatformIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/"
                        + cloudPlatform.getDisplayName() + ".png")));
                cloudPlatformIcon.setFitHeight(30.0);
                cloudPlatformIcon.setFitWidth(30.0);
                GridPane.setRowIndex(cloudPlatformIcon, currentRow);
                GridPane.setColumnIndex(cloudPlatformIcon, 0);

                Label cloudPlatformNameLabel = new Label(cloudPlatform.getDisplayName());
                GridPane.setRowIndex(cloudPlatformNameLabel, currentRow);
                GridPane.setColumnIndex(cloudPlatformNameLabel, 1);

                cloudPlatformParameterPane.getChildren().addAll(cloudPlatformIcon, cloudPlatformNameLabel);

                for (String parameterName : cloudPlatform.getParameter()) { // use cloudPlatform.getUnmodifiableCloudParameterMap() as the map may not contain every params as key and we want it in the order defined
                    String value = viewModel.getCloudPlatformParameterValue(cloudPlatform, parameterName);

                    Label parameterNameLabel = new Label(parameterName);
                    GridPane.setRowIndex(parameterNameLabel, currentRow);
                    GridPane.setColumnIndex(parameterNameLabel, 2);

                    TextField parameterValueLabel = new TextField(Objects.requireNonNullElse(value, ""));
                    parameterValueLabel.textProperty().addListener((observable, oldValue, newValue) -> {
                        viewModel.setCloudPlatformParameter(cloudPlatform, parameterName, newValue);
                    });
                    GridPane.setRowIndex(parameterValueLabel, currentRow);
                    GridPane.setColumnIndex(parameterValueLabel, 3);
                    currentRow++;

                    cloudPlatformParameterPane.getChildren().addAll(parameterNameLabel, parameterValueLabel);
                }
            }
        }
    }
}
