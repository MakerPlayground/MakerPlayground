/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.devicetab;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.device.shared.constraint.IntegerCategoricalConstraint;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SpinnerWithUnit;
import io.makerplayground.ui.control.AzurePropertyControl;
import io.makerplayground.ui.dialog.AzureSettingDialog;
import io.makerplayground.ui.dialog.WarningDialogView;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ActualDeviceConfigView extends VBox{

    private final ConfigActualDeviceViewModel viewModel;

    @FXML private VBox usedDevice;
    @FXML private GridPane usedDeviceSettingPane;
    @FXML private Button autoButton1;
    @FXML private Button autoButton2;
    @FXML private VBox warningPane;
    @FXML private Label warningLabel;
    @FXML private VBox unusedDevice;
    @FXML private GridPane unusedDevicePane;
    @FXML private VBox cloudPlatformParameterSection;
    @FXML private GridPane cloudPlatformParameterPane;
    @FXML private ImageView platFormImage;
    @FXML private Label platformName;
    @FXML private ComboBox<Platform> platFormComboBox;
    @FXML private ComboBox<ActualDeviceComboItem> controllerComboBox;
    @FXML private Label controllerName;
    @FXML private CheckBox optionalPropertyCheckbox1;
    @FXML private CheckBox optionalPropertyCheckbox2;

    private final BooleanProperty showOptionalProperty;
    private final BooleanProperty optionalPropertyCheckBoxVisibleProperty;

    public ActualDeviceConfigView(ConfigActualDeviceViewModel viewModel) {
        this.viewModel = viewModel;
        this.showOptionalProperty = new SimpleBooleanProperty(false);
        this.optionalPropertyCheckBoxVisibleProperty = new SimpleBooleanProperty(false);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/devicetab/ActualDeviceConfigView.fxml"));
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

        warningPane.visibleProperty().bind(Bindings.isEmpty(viewModel.getAllDevices()));
        warningPane.managedProperty().bind(warningPane.visibleProperty());
        if (viewModel.getSelectedController() == null) {
            warningLabel.setText("Select a controller to get started");
        } else if (viewModel.getAllDevices().isEmpty()) {
            warningLabel.setText("Select some devices from the Device Explorer to get started");
        }
    }

    private void initEvent() {
        // redraw when needed
        viewModel.setPlatformChangedCallback(this::initControllerControl);
        viewModel.setControllerChangedCallback(this::initDeviceControl);
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);

        // write change to the viewModel
        platFormComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setPlatform(newValue));
        controllerComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setController(newValue));

        autoButton1.setOnAction(event -> onAutoAction());
        autoButton2.setOnAction(event -> onAutoAction());

        optionalPropertyCheckbox1.selectedProperty().bindBidirectional(showOptionalProperty);
        optionalPropertyCheckbox2.selectedProperty().bindBidirectional(showOptionalProperty);

        autoButton2.visibleProperty().bind(usedDevice.visibleProperty().not());
        autoButton2.managedProperty().bind(usedDevice.visibleProperty().not());

        optionalPropertyCheckbox1.visibleProperty().bind(optionalPropertyCheckBoxVisibleProperty);
        optionalPropertyCheckbox1.managedProperty().bind(optionalPropertyCheckBoxVisibleProperty);

        optionalPropertyCheckbox2.visibleProperty().bind(optionalPropertyCheckBoxVisibleProperty.and(usedDevice.visibleProperty().not()));
        optionalPropertyCheckbox2.managedProperty().bind(optionalPropertyCheckBoxVisibleProperty.and(usedDevice.visibleProperty().not()));
    }

    private void onAutoAction() {
        ProjectMappingResult result = viewModel.autoAssignDevice();
        if (result != ProjectMappingResult.OK) {
            WarningDialogView warningDialogView = new WarningDialogView(getScene().getWindow(), result.getErrorMessage());
            warningDialogView.showAndWait();
        }
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
                            try {
                                ImageView deviceImageView = new ImageView(new Image(Files.newInputStream(
                                        DeviceLibrary.getDeviceThumbnailPath(item.getActualDevice()))));
                                deviceImageView.setFitWidth(75);
                                deviceImageView.setFitHeight(75);
                                deviceImageView.setSmooth(true);
                                deviceImageView.setPreserveRatio(true);

                                HBox hbox = new HBox();
                                hbox.setPrefWidth(75);
                                hbox.setAlignment(Pos.CENTER);
                                hbox.getChildren().add(deviceImageView);

                                setGraphic(hbox);
                                setText(item.getActualDevice().getBrand() + " " + item.getActualDevice().getModel());
                                if (item.getMappingResult() != DeviceMappingResult.OK) {
                                   setOpacity(0.5);
                                }
                            } catch (IOException e) {
                                throw new IllegalStateException("Error: image can't be found");
                            }
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
                    if (item.getMappingResult() != DeviceMappingResult.OK) {
                        setOpacity(0.5);
                    }
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
        if (viewModel.getSelectedController() == null) {
            warningLabel.setText("Select a controller to get started");
        }
    }

    private void initDeviceControl() {
        usedDevice.setVisible(false);
        usedDevice.setManaged(false);
        unusedDevice.setVisible(false);
        unusedDevice.setManaged(false);
        optionalPropertyCheckBoxVisibleProperty.set(false);
        cloudPlatformParameterSection.setVisible(false);
        cloudPlatformParameterSection.setManaged(false);
        if (viewModel.getAllDevices().isEmpty()) {
            warningLabel.setText("Select some devices from the Device Explorer to get started");
        }
        initUsedDeviceControl();
        initUnusedDeviceControl();
        initCloudPlatformPropertyControl();
    }

    private Callback<ListView<CompatibleDeviceComboItem>, ListCell<CompatibleDeviceComboItem>> newDeviceCellFactory() {
        return new Callback<>() {
            @Override
            public ListCell<CompatibleDeviceComboItem> call(ListView<CompatibleDeviceComboItem> param) {
                ListCell<CompatibleDeviceComboItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(CompatibleDeviceComboItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            ActualDevice thumbnailActualDevice = null;
                            // set text
                            if (item.getCompatibleDevice().getActualDevice().isPresent()) {
                                ActualDevice actualDevice = item.getCompatibleDevice().getActualDevice().get();
                                if (actualDevice instanceof IntegratedActualDevice) {
                                    thumbnailActualDevice = ((IntegratedActualDevice) actualDevice).getParent();
                                    setText(actualDevice.getId());
                                } else {
                                    thumbnailActualDevice = actualDevice;
                                    setText(actualDevice.getBrand() + " " + actualDevice.getModel());
                                }
                            } else if (item.getCompatibleDevice().getProjectDevice().isPresent()) {
                                ProjectDevice projectDevice = item.getCompatibleDevice().getProjectDevice().get();
                                thumbnailActualDevice = viewModel.getActualDevice(projectDevice)
                                        .orElseThrow(() -> new IllegalStateException("Actual device of the parent device must have been set"));
                                setText("Use the same device as " + projectDevice.getName());
                            }
                            // set device's display image
                            try {
                                ImageView deviceImageView = new ImageView(new Image(Files.newInputStream(
                                        DeviceLibrary.getDeviceThumbnailPath(thumbnailActualDevice))));
                                deviceImageView.setFitWidth(75);
                                deviceImageView.setFitHeight(75);
                                deviceImageView.setSmooth(true);
                                deviceImageView.setPreserveRatio(true);

                                HBox hbox = new HBox();
                                hbox.setPrefWidth(75);
                                hbox.setAlignment(Pos.CENTER);
                                hbox.getChildren().add(deviceImageView);

                                setGraphic(hbox);
                            } catch (IOException e) {
                                throw new IllegalStateException("Error: image can't be found (" + thumbnailActualDevice + ")");
                            }
                            // grey out and show tooltip if this device isn't compatible
                            if (item.getDeviceMappingResult() != DeviceMappingResult.OK) {
                                setOpacity(0.5);
                                Tooltip tooltip = new Tooltip(item.getDeviceMappingResult().getErrorMessage());
                                tooltip.setShowDelay(Duration.millis(250));
                                setTooltip(tooltip);
                            }
                        }
                    }
                };
                return cell;
            }
        };
    }

    private ListCell<CompatibleDeviceComboItem> newDeviceComboItemListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(CompatibleDeviceComboItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setTooltip(null);
                } else {
                    if (item.getCompatibleDevice().getActualDevice().isPresent()) {
                        ActualDevice actualDevice = item.getCompatibleDevice().getActualDevice().get();
                        if (actualDevice instanceof IntegratedActualDevice) {
                            setText(actualDevice.getId());
                        } else {
                            setText(actualDevice.getBrand() + " " + actualDevice.getModel());
                        }
                    } else if (item.getCompatibleDevice().getProjectDevice().isPresent()) {
                        ProjectDevice projectDevice = item.getCompatibleDevice().getProjectDevice().get();
                        setText("Use the same device as " + projectDevice.getName());
                    }
                    if (item.getDeviceMappingResult() != DeviceMappingResult.OK) {
                        setOpacity(0.5);
                    }
                }
            }
        };
    }

    private ListCell<Connection> newConnectionListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(empty ? null : item.getName());
                    if (item.getType() == ConnectionType.INTEGRATED) {
                        setText(empty ? null : "Yes");
                    }
                } else {
                    setText(null);
                }
            }
        };
    }

    private Callback<ListView<Connection>, ListCell<Connection>> newConnectionCellFactory() {
        return new Callback<>() {
            @Override
            public ListCell<Connection> call(ListView<Connection> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Connection item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(empty ? null : item.getName());
                            if (item.getType() == ConnectionType.INTEGRATED) {
                                setText(empty ? null : "Yes");
                            }
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
    }

    private void initDeviceSettingControl(GridPane settingPane, Set<ProjectDevice> devices) {
        settingPane.getChildren().clear();
        int currentRow = 0;
        for (ProjectDevice projectDevice : devices) {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/"
                    + projectDevice.getGenericDevice().getName() + ".png")));
            imageView.setFitHeight(30.0);
            imageView.setFitWidth(30.0);
            GridPane.setConstraints(imageView, 0, currentRow, 1, 1, HPos.LEFT, VPos.TOP);

            TextField nameTextField = new TextField();
            nameTextField.setMinHeight(25); // a hack to center the label to the height of 1 row control when the control spans to multiple rows
            nameTextField.setId("nameTextField");
            nameTextField.setText(projectDevice.getName());
            nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    Project.SetNameResult result = viewModel.setProjectDeviceName(projectDevice, nameTextField.getText());
                    if (result != Project.SetNameResult.OK) {
                        Point2D textFieldPosition = nameTextField.localToScreen(0, 0);
                        Tooltip tooltip = new Tooltip(result.getErrorMessage());
                        tooltip.setAutoHide(true);
                        tooltip.show(nameTextField, textFieldPosition.getX() + 3
                                , textFieldPosition.getY() + nameTextField.getBoundsInLocal().getHeight() + 3);
                        new Timeline(new KeyFrame(Duration.seconds(2), event -> tooltip.hide())).play();

                        nameTextField.setText(projectDevice.getName());
                    }
                }
            });
            GridPane.setConstraints(nameTextField, 1, currentRow, 1, 1, HPos.LEFT, VPos.TOP);

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
            CompatibleDeviceComboItem selectedItem = deviceComboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getDeviceMappingResult() != DeviceMappingResult.OK) {
                Tooltip tooltip = new Tooltip(selectedItem.getDeviceMappingResult().getErrorMessage());
                tooltip.setShowDelay(Duration.millis(250));
                deviceComboBox.setTooltip(tooltip);
            }

            VBox entireComboBoxDevice = new VBox();
            entireComboBoxDevice.setSpacing(10.0);
            entireComboBoxDevice.setId("entireComboBoxDevice");
            entireComboBoxDevice.getChildren().addAll(deviceComboBox);
            GridPane.setConstraints(entireComboBoxDevice, 2, currentRow, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.SOMETIMES);

            ImageView removeButton = new ImageView(new Image(getClass().getResourceAsStream("/icons/device-delete.png")));
            removeButton.setOnMousePressed(event -> viewModel.removeDevice(projectDevice));
            removeButton.setFitHeight(25.0);
            removeButton.setFitWidth(25.0);
            GridPane.setConstraints(removeButton, 3, currentRow, 1, 1, HPos.LEFT, VPos.TOP);

            if (deviceComboBox.getSelectionModel().getSelectedItem() != null) {
                CompatibleDevice compatibleDevice = deviceComboBox.getSelectionModel().getSelectedItem().getCompatibleDevice();
                if (deviceComboBox.getSelectionModel().getSelectedItem().getDeviceMappingResult() == DeviceMappingResult.OK) {
                    compatibleDevice.getActualDevice().ifPresent(actualDevice -> {
                        if (actualDevice.getDeviceType() == DeviceType.VIRTUAL) {
                            return;
                        }
                        FlowPane portListUI = new FlowPane(Orientation.HORIZONTAL, 5.0, 5.0);
                        Map<Connection, List<Connection>> possibleDeviceConnection = viewModel.getPossibleDeviceConnections(projectDevice, actualDevice);
                        for (Connection connectionConsume: possibleDeviceConnection.keySet()) {
                            List<Connection> possibleConnectionProvide = new ArrayList<>(possibleDeviceConnection.get(connectionConsume));
                            possibleConnectionProvide.add(0, null);
                            ComboBox<Connection> portComboBox = new ComboBox<>(FXCollections.observableList(possibleConnectionProvide));
                            portComboBox.setCellFactory(newConnectionCellFactory());
                            portComboBox.setButtonCell(newConnectionListCell());

                            Connection connection = viewModel.getSelectedConnection(projectDevice, connectionConsume);
                            portComboBox.getSelectionModel().select(connection);
                            portComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> viewModel.setConnection(projectDevice, connectionConsume, newValue));

                            Label portLabel = new Label(connectionConsume.getType() == ConnectionType.INTEGRATED ? "Connect" : connectionConsume.getName());
                            Tooltip tooltip = new Tooltip(connectionConsume.getName());
                            tooltip.setShowDelay(Duration.ZERO);
                            portLabel.setTooltip(tooltip);
                            portLabel.setMinWidth(Region.USE_PREF_SIZE);
                            portListUI.setAlignment(Pos.CENTER_LEFT);
                            HBox hbox = new HBox(5.0);
                            hbox.setAlignment(Pos.CENTER_LEFT);
                            hbox.getChildren().addAll(portLabel, portComboBox);
                            portListUI.getChildren().addAll(hbox);
                        }
                        entireComboBoxDevice.getChildren().add(portListUI);
                    });
                }
            }

            // We only show port combobox and property textfield when the device has been selected
            if (viewModel.isActualDevicePresent(projectDevice)) {

                // property
                if (viewModel.getActualDevice(projectDevice).orElseThrow().getProperty() != null && !viewModel.getActualDevice(projectDevice).orElseThrow().getProperty().isEmpty()) {
                    GridPane propertyGridPane = new GridPane();
                    propertyGridPane.setHgap(10);
                    propertyGridPane.setVgap(10);

                    List<Property> propertyList = viewModel.getActualDevice(projectDevice).orElseThrow().getProperty();
                    for (int i=0; i<propertyList.size(); i++) {
                        Property p = propertyList.get(i);

                        if (p.isOptional()) {
                            optionalPropertyCheckBoxVisibleProperty.set(true);
                        }

                        BooleanBinding showProperty = new SimpleBooleanProperty(p.isOptional()).not().or(showOptionalProperty);

                        Label propertyLabel = new Label(p.getName());
                        GridPane.setRowIndex(propertyLabel, i);
                        GridPane.setColumnIndex(propertyLabel, 0);
                        propertyLabel.visibleProperty().bind(showProperty);
                        propertyLabel.managedProperty().bind(showProperty);
                        propertyGridPane.getChildren().add(propertyLabel);

                        Object currentValue = viewModel.getPropertyValue(projectDevice, p);
                        if (p.getDataType() == DataType.STRING && p.getControlType() == ControlType.TEXTBOX) {
                            TextField textField = new TextField((String) currentValue);
                            textField.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(textField, i);
                            GridPane.setColumnIndex(textField, 1);
                            textField.visibleProperty().bind(showProperty);
                            textField.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(textField);
                        } else if (p.getDataType() == DataType.INTEGER_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            // TODO: we should create a variant of CategoricalConstraint that support list of other type instead of String
                            ObservableList<Integer> list = FXCollections.observableArrayList(((IntegerCategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<Integer> comboBox = new ComboBox<>(list);
                            if (currentValue == null) {
                                comboBox.getSelectionModel().select(null);
                            } else {
                                comboBox.getSelectionModel().select((Integer) currentValue);
                            }
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            comboBox.visibleProperty().bind(showProperty);
                            comboBox.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(comboBox);
                        } else if (p.getDataType() == DataType.BOOLEAN_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            if (currentValue == null) {
                                comboBox.getSelectionModel().select(null);
                            } else {
                                comboBox.getSelectionModel().select(String.valueOf(currentValue));
                            }
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, Boolean.parseBoolean(newValue)));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            comboBox.visibleProperty().bind(showProperty);
                            comboBox.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(comboBox);
                        } else if ((p.getDataType() == DataType.INTEGER || p.getDataType() == DataType.DOUBLE)
                                && p.getControlType() == ControlType.SPINBOX) {
                            SpinnerWithUnit spinner = new SpinnerWithUnit(p.getMinimumValue(), p.getMaximumValue()
                                    , List.of(p.getUnit()), (NumberWithUnit) currentValue);
                            spinner.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(spinner, i);
                            GridPane.setColumnIndex(spinner, 1);
                            spinner.visibleProperty().bind(showProperty);
                            spinner.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(spinner);
                        } else if (p.getDataType() == DataType.AZURE_COGNITIVE_KEY && p.getControlType() == ControlType.AZURE_WIZARD) {
                            AzurePropertyControl<AzureCognitiveServices> control = new AzurePropertyControl<>(AzureSettingDialog.Service.COGNITIVE_SERVICE
                                    , (AzureCognitiveServices) currentValue);
                            control.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(control, i);
                            GridPane.setColumnIndex(control, 1);
                            control.visibleProperty().bind(showProperty);
                            control.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(control);
                        } else if (p.getDataType() == DataType.AZURE_IOTHUB_KEY && p.getControlType() == ControlType.AZURE_WIZARD) {
                            AzurePropertyControl<AzureIoTHubDevice> control = new AzurePropertyControl<>(AzureSettingDialog.Service.IOT_HUB
                                    , (AzureIoTHubDevice) currentValue);
                            control.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(control, i);
                            GridPane.setColumnIndex(control, 1);
                            control.visibleProperty().bind(showProperty);
                            control.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(control);
                        } else if (p.getDataType() == DataType.STRING_INT_ENUM && p.getControlType() == ControlType.DROPDOWN) {
                            Map<String, Integer> map = ((StringIntegerCategoricalConstraint) p.getConstraint()).getMap();
                            ObservableList<String> list = FXCollections.observableArrayList(FXCollections.observableArrayList(map.keySet()));
                            ComboBox<String> comboBox = new ComboBox<>(list);
                            if (currentValue == null) {
                                comboBox.getSelectionModel().select(null);
                            } else {
                                comboBox.getSelectionModel().select(String.valueOf(currentValue));
                            }
                            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setPropertyValue(projectDevice, p, newValue));
                            GridPane.setRowIndex(comboBox, i);
                            GridPane.setColumnIndex(comboBox, 1);
                            comboBox.visibleProperty().bind(showProperty);
                            comboBox.managedProperty().bind(showProperty);
                            propertyGridPane.getChildren().add(comboBox);
                        } else {    // TODO: add support for new property type
                            throw new IllegalStateException("Found unknown property type");
                        }
                    }
                    entireComboBoxDevice.getChildren().add(propertyGridPane);
                }
            }

            settingPane.getChildren().addAll(imageView, nameTextField, entireComboBoxDevice, removeButton);
            currentRow++;
        }
    }

    private void initUsedDeviceControl() {
        if (viewModel.getUsedDevice().isEmpty()) {
            usedDevice.setVisible(false);
            usedDevice.setManaged(false);
        } else {
            usedDevice.setVisible(true);
            usedDevice.setManaged(true);
        }

        viewModel.clearDeviceConfigChangedCallback();
        initDeviceSettingControl(usedDeviceSettingPane, viewModel.getUsedDevice());
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);
    }

    private void initUnusedDeviceControl() {
        if (viewModel.getUnusedDevice().isEmpty()) {
            unusedDevice.setVisible(false);
            unusedDevice.setManaged(false);
        } else {
            unusedDevice.setVisible(true);
            unusedDevice.setManaged(true);
        }

        unusedDevicePane.getChildren().clear();
        viewModel.clearDeviceConfigChangedCallback();
        initDeviceSettingControl(unusedDevicePane, viewModel.getUnusedDevice());
        viewModel.setDeviceConfigChangedCallback(this::initDeviceControl);
    }

    private void initCloudPlatformPropertyControl() {
        if (viewModel.getAllCloudPlatforms().isEmpty()) {
            cloudPlatformParameterSection.setVisible(false);
            cloudPlatformParameterSection.setManaged(false);
        } else {
            cloudPlatformParameterSection.setVisible(true);
            cloudPlatformParameterSection.setManaged(true);

            int currentRow = 0;
            cloudPlatformParameterPane.getChildren().clear();
            for (CloudPlatform cloudPlatform : viewModel.getAllCloudPlatforms()) {
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
