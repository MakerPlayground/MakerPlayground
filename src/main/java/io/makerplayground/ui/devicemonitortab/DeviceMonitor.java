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

package io.makerplayground.ui.devicemonitortab;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import io.makerplayground.generator.upload.UploadTarget;
import io.makerplayground.generator.upload.UploadMode;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceMonitor extends SplitPane {

    private static final Pattern format = Pattern.compile("(\\[\\[I]]|\\[\\[E]]|\\[\\[V]])?\\u0000\"(.*)\"\\u0000(.+)", Pattern.DOTALL); // Regex
    private static final Pattern numberRegex = Pattern.compile("^(-?\\d+\\.\\d+)$|^(-?\\d+)$");

    private ReadOnlyBooleanWrapper isRunning = new ReadOnlyBooleanWrapper(false);

    private final ObservableList<LogItem> logData = FXCollections.observableArrayList();
    private final FilteredList<LogItem> logDataFilter = new FilteredList<>(logData);
    private final Map<String, LineChart<Number, Number>> lineChartMap = new HashMap<>();

    private final ObservableList<String> tagListForTable = FXCollections.observableArrayList();
    private final ObservableList<String> tagListForChart = FXCollections.observableArrayList();

    @FXML private TableView<LogItem> deviceMonitorTable;
    @FXML private ComboBox<LogLevel> levelComboBox;
    @FXML private CheckComboBox<String> checkTagComboBox;
    @FXML private CheckBox autoScrollCheckbox;
    @FXML private CheckComboBox<String> plotTagComboBox;
    @FXML private VBox chartPane;
    @FXML private Button clearTableButton;
    @FXML private Button clearChartButton;

    private UploadTarget currentUploadTarget;
    private SerialPort serialPort;
    private WebSocketClient webSocketClient;

    public DeviceMonitor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/DeviceMonitor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        // initialize filter ui

        levelComboBox.getItems().addAll(FXCollections.observableArrayList(LogLevel.values()));
        levelComboBox.getSelectionModel().select(0);
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(observable -> updateLogFilter());

        Bindings.bindContent(checkTagComboBox.getItems(), tagListForTable);
        checkTagComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) observable -> updateLogFilter());

        updateLogFilter();

        // initialize table

        TableColumn<LogItem, String> deviceNameTableColumn = new TableColumn<>("Device Name");
        deviceNameTableColumn.setPrefWidth(130);
        deviceNameTableColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDeviceName()));

        TableColumn<LogItem, String> messageTableColumn = new TableColumn<>("Message");
        messageTableColumn.setPrefWidth(300);
        messageTableColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getMessage()));

        deviceMonitorTable.getColumns().addAll(deviceNameTableColumn, messageTableColumn);
        deviceMonitorTable.setItems(logDataFilter);

        clearTableButton.setOnAction(event -> {
            checkTagComboBox.getCheckModel().clearChecks();
            tagListForTable.clear();
            logData.clear();
        });

        // initialize plot

        Bindings.bindContent(plotTagComboBox.getItems(), tagListForChart);

        plotTagComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) observable -> {
            ObservableList<String> tagSelected = plotTagComboBox.getCheckModel().getCheckedItems();

            Set<String> tagToBeRemoved = new HashSet<>(tagListForChart);
            tagToBeRemoved.removeAll(tagSelected);

            for (String tag : tagToBeRemoved) {
                if (lineChartMap.containsKey(tag)) {
                    chartPane.getChildren().remove(lineChartMap.remove(tag));
                }
            }

            for (String tag : tagSelected) {
                if (!lineChartMap.containsKey(tag)) {
                    createLineChart(tag);
                }
            }
        });

        clearChartButton.setOnAction(event -> {
            plotTagComboBox.getCheckModel().clearChecks();
            tagListForChart.clear();
        });
    }

    private void updateLogFilter() {
        ObservableList<String> tagList = checkTagComboBox.getCheckModel().getCheckedItems();
        int selectedPriority = levelComboBox.getSelectionModel().getSelectedItem().getPriority();
        logDataFilter.setPredicate(logItem -> tagList.contains(logItem.getDeviceName())
                && logItem.getLevel().getPriority() >= selectedPriority);
    }

    private void createLineChart(String tag) {
        Map<String, XYChart.Series<Number, Number>> valueSeriesMap = new HashMap<>();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Samples");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(tag);
        lineChart.setAnimated(false);

        logData.addListener((ListChangeListener<LogItem>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new UnsupportedOperationException();
                } else if (c.wasUpdated()) {
                    throw new UnsupportedOperationException();
                } else {
                    for (LogItem removedItem : c.getRemoved()) {
//                        throw new UnsupportedOperationException();
                    }
                    for (LogItem addedItem : c.getAddedSubList()) {
                        if (addedItem.getDeviceName().equals(tag)) {
                            // assume that the format is "A=10.5,B=5,C=2.3"
                            String[] valuePairs = addedItem.getMessage().split(",");
                            for (String valuePair : valuePairs) {
                                String[] tokens = valuePair.split("=");
                                String valueName = tokens[0];
                                Matcher matcher = numberRegex.matcher(tokens[1]);
                                if (matcher.matches()) {
                                    double value = Double.parseDouble(tokens[1]);
                                    if (!valueSeriesMap.containsKey(valueName)) {
                                        XYChart.Series<Number, Number> series = new XYChart.Series<>();
                                        series.setName(valueName);
                                        valueSeriesMap.put(valueName, series);
                                        lineChart.getData().add(series);
                                    }
                                    XYChart.Series<Number, Number> series = valueSeriesMap.get(valueName);
                                    series.getData().add(new XYChart.Data<>(series.getData().size(), value));
                                }
                            }
                        }
                    }
                }
            }
        });

        lineChartMap.put(tag, lineChart);
        chartPane.getChildren().add(lineChart);
    }

    public boolean startMonitor(UploadTarget uploadTarget) {
        currentUploadTarget = uploadTarget;
        if (uploadTarget.getUploadMode().equals(UploadMode.SERIAL_PORT)) {
            return startMonitor(uploadTarget.getSerialPort());
        } else if (uploadTarget.getUploadMode().equals(UploadMode.RPI_ON_NETWORK)){
            return startMonitor(uploadTarget.getRpiHostName());
        } else {
            return false;
        }
    }

    private void processMessageIn(String message) {
        Matcher log = format.matcher(message);
        if (log.find() && log.groupCount() == 3) {
            LogItem logItem = new LogItem(log.group(1), log.group(2), log.group(3));
            Platform.runLater(() -> {
                logData.addAll(logItem);
                if (!tagListForTable.contains(logItem.getDeviceName())) {
                    tagListForTable.add(logItem.getDeviceName());
                    // A hack to make the checkbox tick became visible properly
                    Platform.runLater(() -> checkTagComboBox.getCheckModel().check(logItem.getDeviceName()));
                }
                if (autoScrollCheckbox.isSelected()) {
                    deviceMonitorTable.scrollTo(logItem);
                }

                if (!tagListForChart.contains(logItem.getDeviceName())) {
                    tagListForChart.add(logItem.getDeviceName());
                    // A hack to make the checkbox tick became visible properly
                    Platform.runLater(() -> plotTagComboBox.getCheckModel().check(logItem.getDeviceName()));
                }
            });
        }
    }

    private boolean startMonitor(String rpiHostName) {
        URI uri = URI.create("ws://" + rpiHostName + ":6213");
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {

            }

            @Override
            public void onMessage(String message) {
                String[] lines = message.strip().split("\n");
                for (String line : lines) {
                    processMessageIn(line);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {

            }
        };
        try {
            if (webSocketClient.connectBlocking(5, TimeUnit.SECONDS)) {
                isRunning.set(true);
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean startMonitor(SerialPort port) {
        serialPort = port;
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.addDataListener(new SerialPortMessageListener() {

            @Override
            public byte[] getMessageDelimiter() {
                return new byte[]{'\r'};
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                processMessageIn(new String(event.getReceivedData()).strip());
            }
        });
        if (!serialPort.openPort()) {
            return false;
        }
        isRunning.set(true);
        return true;
    }

    public void stopMonitor() {
        if (currentUploadTarget == null) {
            return;
        }
        if (currentUploadTarget.getUploadMode().equals(UploadMode.SERIAL_PORT)) {
            if (serialPort != null) {
                serialPort.removeDataListener();
                isRunning.set(false);
                serialPort.closePort();
            }
        } else if (currentUploadTarget.getUploadMode().equals(UploadMode.RPI_ON_NETWORK)) {
            if (webSocketClient != null) {
                isRunning.set(false);
                webSocketClient.close();
            }
        } else {
            throw new IllegalStateException("There is no implementation for " + currentUploadTarget);
        }
    }

}