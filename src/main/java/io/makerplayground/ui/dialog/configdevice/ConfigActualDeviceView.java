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
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigActualDeviceView extends VBox{

    private final ConfigActualDeviceViewModel viewModel;

    @FXML private GridPane usedDevice;
    @FXML private FlowPane unusedDevicePane;
    @FXML private VBox unusedDevice;
    @FXML private VBox cloudPlatformParameterSection;
    @FXML private GridPane cloudPlatformParameterPane;
    @FXML private ImageView platFormImage;
    @FXML private Label platformName;
    @FXML private ComboBox<Platform> platFormComboBox;
    @FXML private ComboBox<ActualDevice> controllerComboBox;
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

        // write change to the viewmodel
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
            public ListCell<ActualDevice> call(ListView<ActualDevice> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ActualDevice item, boolean empty) {
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
            protected void updateItem(ActualDevice item, boolean empty) {
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
        unusedDevice.setVisible(false);
        unusedDevice.setManaged(false);
        cloudPlatformParameterSection.setVisible(false);
        cloudPlatformParameterSection.setManaged(false);
        initDeviceControlChildren();
        initUnusedDeviceControl();
        initCloudPlatformPropertyControl();
    }

    private void initDeviceControlChildren() {
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
            ComboBox<ActualDevice> deviceComboBox = new ComboBox<>(FXCollections.observableList(viewModel.getCompatibleDevice(projectDevice)));
            deviceComboBox.setId("deviceComboBox");
            deviceComboBox.setCellFactory(new Callback<>() {
                @Override
                public ListCell<ActualDevice> call(ListView<ActualDevice> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ActualDevice item, boolean empty) {
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
                protected void updateItem(ActualDevice item, boolean empty) {
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

            VBox entireComboBoxDevice = new VBox();
            entireComboBoxDevice.setSpacing(10.0);
            entireComboBoxDevice.setId("entireComboBoxDevice");
            entireComboBoxDevice.setDisable(viewModel.getController() == null);
            entireComboBoxDevice.getChildren().addAll(deviceComboBox);
            GridPane.setConstraints(entireComboBoxDevice, 2, currentRow, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.SOMETIMES);

            HBox portComboBoxHbox = new HBox();
            portComboBoxHbox.setSpacing(5.0);
            portComboBoxHbox.setAlignment(Pos.CENTER_LEFT);

            Map<Peripheral, List<List<DevicePort>>> combo = viewModel.getCompatiblePort(projectDevice);
            // We only show port combobox and property textfield when the device has been selected
            ActualDevice actualDevice = projectDevice.getActualDevice();
            if (actualDevice != null) {
                if (!actualDevice.getPort(Peripheral.NOT_CONNECTED).isEmpty()) {
                    viewModel.setPeripheral(projectDevice, Peripheral.NOT_CONNECTED, Collections.emptyList());
                } else {
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
                            // add dummy value to position 1 (after the selected item) to allow the selected port to be cleared
                            portComboBox.getItems().add(1, Collections.emptyList());
                            portComboBox.getSelectionModel().select(projectDevice.getDeviceConnection().get(p));
                        } else {
                            // add dummy value to allow the selected port to be cleared
                            portComboBox.getItems().add(0, Collections.emptyList());
                            portComboBox.getSelectionModel().select(Collections.emptyList());
                        }
                        portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.isEmpty()) {
                                viewModel.clearPeripheral(projectDevice, p);
                            } else {
                                viewModel.setPeripheral(projectDevice, p, newValue);
                            }
                        });

                        // TODO: handle other type (UART, SPI, etc.)
                        String portName;
                        if (p.getConnectionType() == ConnectionType.I2C) {
                            portName = "I2C";
                        } else {
                            portName = projectDevice.getActualDevice().getPort(p).get(0).getName();
                        }

                        portComboBoxHbox.getChildren().addAll(new Label(portName), portComboBox);
                    }
                    entireComboBoxDevice.getChildren().add(portComboBoxHbox);
                }

                // property
                if (!projectDevice.getActualDevice().getProperty().isEmpty()) {
                    GridPane propertyGridPane = new GridPane();
                    propertyGridPane.setHgap(10);
                    propertyGridPane.setVgap(10);

                    List<Property> propertyList = projectDevice.getActualDevice().getProperty();
                    for (int i=0; i<propertyList.size(); i++) {
                        Property p = propertyList.get(i);

                        Label propertyLabel = new Label(p.getName());
                        GridPane.setRowIndex(propertyLabel, i);
                        GridPane.setColumnIndex(propertyLabel, 0);
                        propertyGridPane.getChildren().add(propertyLabel);

                        if (p.getDataType() == DataType.STRING && p.getControlType() == ControlType.TEXTBOX) {
                            TextField textField = new TextField(projectDevice.getPropertyValue(p));
                            textField.textProperty().addListener((observable, oldValue, newValue) -> projectDevice.setPropertyValue(p, newValue));
                            GridPane.setRowIndex(textField, i);
                            GridPane.setColumnIndex(textField, 1);
                            propertyGridPane.getChildren().add(textField);
                        } else if (p.getDataType() == DataType.INTEGER_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> projectDevice.setPropertyValue(p, newValue));
                            if (projectDevice.getPropertyValue(p) == null) {
                                projectDevice.setPropertyValue(p, p.getDefaultValue().toString());
                            }
                            comboBox.getSelectionModel().select(projectDevice.getPropertyValue(p));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            propertyGridPane.getChildren().add(comboBox);
                        } else {    // TODO: add support for new property type
                            throw new IllegalStateException("Found unknown property type");
                        }
                    }
                    entireComboBoxDevice.getChildren().add(propertyGridPane);
                }
            }

            usedDevice.getChildren().addAll(imageView, name, entireComboBoxDevice);
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

                for (String parameterName : cloudPlatform.getParameter()) { // use cloudPlatform.getParameter() as the map may not contain every params as key and we want it in the order defined
                    String value = viewModel.getCloudPlatfromParameterValue(cloudPlatform, parameterName);

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
