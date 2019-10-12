/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

package io.makerplayground.ui.dialog;

import io.makerplayground.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class AzureSettingDialog<T extends AzureResource> extends UndecoratedDialog {

    private StackPane mainPane = new StackPane();
    @FXML private VBox statusPane;
    @FXML private Label statusLabel;
    @FXML private VBox settingPane;
    @FXML private Label accountLabel;
    @FXML private Button signInButton;
    @FXML private ComboBox<AzureSubscription> subscriptionCombobox;
    @FXML private ComboBox<AzureResourceGroup> resourceGroupCombobox;
    @FXML private Label iotHubLabel;
    @FXML private ComboBox<AzureIoTHub> iotHubCombobox;
    @FXML private Label resultLabel;
    @FXML private ComboBox<T> resultCombobox;

    private ObservableList<AzureSubscription> subscriptions = FXCollections.observableArrayList();
    private ObservableList<AzureResourceGroup> resourceGroups = FXCollections.observableArrayList();
    private ObservableList<AzureIoTHub> iotHubs = FXCollections.observableArrayList();
    private ObservableList<T> results = FXCollections.observableArrayList();
    private State state = State.NOT_LOGIN;

    private final Service service;
    private T result;

    private enum State {
        NOT_LOGIN,
        LOGIN,
        SUBSCRIPTION_SELECTED,
        RESOUCRGROUP_SELECTED,
        IOTHUB_SELECTED
    }

    public enum Service {
        IOT_HUB,
        COGNITIVE_SERVICE
    }

    public AzureSettingDialog(Service service, Window owner) {
        super(owner);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/AzureSettingDialog.fxml"));
        fxmlLoader.setRoot(mainPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setContent(mainPane);
        this.service = service;

        statusPane.managedProperty().bind(statusPane.visibleProperty());
        settingPane.managedProperty().bind(settingPane.visibleProperty());
        iotHubLabel.managedProperty().bind(iotHubLabel.visibleProperty());
        iotHubCombobox.managedProperty().bind(iotHubCombobox.visibleProperty());

        initUI();

        // check for authentication status before initialize ui
        showProgressIndicator("Checking for authentication status...");
        AzureManagement.ListSubscriptionTask listSubscriptionTask = new AzureManagement.ListSubscriptionTask();
        listSubscriptionTask.setOnSucceeded(event -> {
            if (!listSubscriptionTask.getErrorMessage().isEmpty()) {
                state = State.NOT_LOGIN;
            } else {
                subscriptions.setAll(listSubscriptionTask.getValue());
                state = State.LOGIN;
            }
            hideProgressIndicator();
            updateUI();
        });
        new Thread(listSubscriptionTask).start();
    }

    private void showProgressIndicator(String message) {
        statusPane.setVisible(true);
        statusLabel.setText(message);
        settingPane.setVisible(false);
    }

    private void hideProgressIndicator() {
        statusPane.setVisible(false);
        settingPane.setVisible(true);
    }

    private void initUI() {
        signInButton.setOnAction(event -> {
            signInButton.setDisable(true);

            if (state == State.NOT_LOGIN) {
                signInButton.setText("Signing in...");
                sizeToScene();
                AzureManagement.LogInTask logInTask = new AzureManagement.LogInTask();
                logInTask.setOnSucceeded(event1 -> {
                    state = State.LOGIN;
                    subscriptions.setAll(logInTask.getValue());
                    updateUI();
                });
                new Thread(logInTask).start();
            } else {
                signInButton.setText("Signing out...");
                sizeToScene();
                AzureManagement.LogOutTask logOutTask = new AzureManagement.LogOutTask();
                logOutTask.setOnSucceeded(event1 -> {
                    state = State.NOT_LOGIN;
                    subscriptions.clear();
                    updateUI();
                });
                new Thread(logOutTask).start();
            }
        });

        subscriptionCombobox.setItems(subscriptions);
        setComboboxDisplay(subscriptionCombobox, AzureSubscription::getName);
        subscriptionCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                state = State.SUBSCRIPTION_SELECTED;
                // update list of resource group when subscription is changed
                showProgressIndicator("Listing resource group...");
                AzureManagement.ResourceGroupListTask groupList = new AzureManagement.ResourceGroupListTask(newValue);
                groupList.setOnSucceeded(event -> {
                    resourceGroups.setAll(groupList.getValue());
                    hideProgressIndicator();
                    updateUI();
                });
                new Thread(groupList).start();
            }
        });

        resourceGroupCombobox.setItems(resourceGroups);
        setComboboxDisplay(resourceGroupCombobox, AzureResourceGroup::getName);
        resourceGroupCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                state = State.RESOUCRGROUP_SELECTED;
                if (service == Service.IOT_HUB) {
                    // update list of cognitive service
                    showProgressIndicator("Listing IoT Hub...");
                    AzureManagement.IotHubListTask iotHubListTask = new AzureManagement.IotHubListTask(subscriptionCombobox.getValue()
                            , resourceGroupCombobox.getValue());
                    iotHubListTask.setOnSucceeded(event -> {
                        iotHubs.setAll(iotHubListTask.getValue());
                        hideProgressIndicator();
                        updateUI();
                    });
                    new Thread(iotHubListTask).start();
                } else if (service == Service.COGNITIVE_SERVICE) {
                    // update list of cognitive service
                    showProgressIndicator("Listing cognitive services...");
                    AzureManagement.CognitiveListTask cognitiveList = new AzureManagement.CognitiveListTask(subscriptionCombobox.getValue()
                            , resourceGroupCombobox.getValue());
                    cognitiveList.setOnSucceeded(event -> {
                        results.clear();
                        cognitiveList.getValue().forEach(azureCognitiveServices -> results.add((T) azureCognitiveServices));    // this is a necessary safe unchecked cast
                        hideProgressIndicator();
                        updateUI();
                    });
                    new Thread(cognitiveList).start();
                }
            }
        });

        if (service == Service.IOT_HUB) {
            iotHubLabel.setText("IoT Hub");
            resultLabel.setText("IoT Hub Device");
        } else if (service == Service.COGNITIVE_SERVICE) {
            iotHubLabel.setVisible(false);
            iotHubCombobox.setVisible(false);
            resultLabel.setText("Cognitive Services");
        }

        iotHubCombobox.setItems(iotHubs);
        setComboboxDisplay(iotHubCombobox, AzureIoTHub::getName);
        iotHubCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // update list of cognitive service
            state = State.IOTHUB_SELECTED;
            showProgressIndicator("Listing IoT Hub Devices...");
            AzureManagement.IotHubDeviceListTask iotHubDeviceListTask = new AzureManagement.IotHubDeviceListTask(iotHubCombobox.getValue()
                    , subscriptionCombobox.getValue(), resourceGroupCombobox.getValue());
            iotHubDeviceListTask.setOnSucceeded(event -> {
                results.clear();
                iotHubDeviceListTask.getValue().forEach(ioTHubDevice -> results.add((T) ioTHubDevice));    // this is a necessary safe unchecked cast
                hideProgressIndicator();
                updateUI();
            });
            new Thread(iotHubDeviceListTask).start();
        });

        resultCombobox.setItems(results);
        setComboboxDisplay(resultCombobox, T::getName);
        resultCombobox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                result = null;
            } else if (service == Service.IOT_HUB) {
                AzureIoTHubDevice azureIoTHubDevice = (AzureIoTHubDevice) newValue;
                // get list of device of the selected iot hub
                showProgressIndicator("Getting settings for the selected services...");
                AzureManagement.IotHubShowConnectionString showConnectionStringTask = new AzureManagement.IotHubShowConnectionString(
                        subscriptionCombobox.getValue(), resourceGroupCombobox.getValue(), iotHubCombobox.getValue(), azureIoTHubDevice);
                showConnectionStringTask.setOnSucceeded(event -> {
                    result = (T) showConnectionStringTask.getValue();
                    hideProgressIndicator();
                });
                new Thread(showConnectionStringTask).start();
            } else if (service == Service.COGNITIVE_SERVICE) {
                AzureCognitiveServices azureCognitiveServices = (AzureCognitiveServices) newValue;
                // get key for the selected cognitive service
                showProgressIndicator("Getting settings for the selected services...");
                AzureManagement.CognitiveKeyListTask cognitiveKeyListTask = new AzureManagement.CognitiveKeyListTask(
                        subscriptionCombobox.getValue(), resourceGroupCombobox.getValue(), azureCognitiveServices);
                cognitiveKeyListTask.setOnSucceeded(event -> {
                    result = (T) cognitiveKeyListTask.getValue();
                    hideProgressIndicator();
                });
                new Thread(cognitiveKeyListTask).start();
            }
        });
    }

    private void updateUI() {
        if (state == State.NOT_LOGIN) {
            accountLabel.setText("-");
            signInButton.setText("Sign In");
            signInButton.setDisable(false);
            subscriptionCombobox.getSelectionModel().clearSelection();
            subscriptionCombobox.setDisable(true);
            resourceGroupCombobox.getSelectionModel().clearSelection();
            resourceGroupCombobox.setDisable(true);
            iotHubCombobox.getSelectionModel().clearSelection();
            iotHubCombobox.setDisable(true);
            resultCombobox.getSelectionModel().clearSelection();
            resultCombobox.setDisable(true);
        } else if (state == State.LOGIN) {
            accountLabel.setText(subscriptions.get(0).getUserName());
            signInButton.setText("Sign Out");
            signInButton.setDisable(false);
            subscriptionCombobox.getSelectionModel().clearSelection();
            subscriptionCombobox.setDisable(false);
            resourceGroupCombobox.getSelectionModel().clearSelection();
            resourceGroupCombobox.setDisable(true);
            iotHubCombobox.getSelectionModel().clearSelection();
            iotHubCombobox.setDisable(true);
            resultCombobox.getSelectionModel().clearSelection();
            resultCombobox.setDisable(true);
        } else if (state == State.SUBSCRIPTION_SELECTED) {
            resourceGroupCombobox.getSelectionModel().clearSelection();
            resourceGroupCombobox.setDisable(false);
            iotHubCombobox.getSelectionModel().clearSelection();
            iotHubCombobox.setDisable(true);
            resultCombobox.getSelectionModel().clearSelection();
            resultCombobox.setDisable(true);
        } else if (state == State.RESOUCRGROUP_SELECTED) {
            if (service == Service.IOT_HUB) {
                iotHubCombobox.getSelectionModel().clearSelection();
                iotHubCombobox.setDisable(false);
                resultCombobox.getSelectionModel().clearSelection();
                resultCombobox.setDisable(true);
            } else {
                resultCombobox.getSelectionModel().clearSelection();
                resultCombobox.setDisable(false);
            }
        } else if (state == State.IOTHUB_SELECTED) {
            resultCombobox.getSelectionModel().clearSelection();
            resultCombobox.setDisable(false);
        } else {
            throw new IllegalStateException();
        }

        Platform.runLater(this::sizeToScene);
    }

    public Optional<T> getResult() {
        return Optional.ofNullable(result);
    }

    private <T> void setComboboxDisplay(ComboBox<T> comboBox, Function<T, String> converter) {
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(converter.apply(item));
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(converter.apply(item));
                }
            }
        });
    }
}