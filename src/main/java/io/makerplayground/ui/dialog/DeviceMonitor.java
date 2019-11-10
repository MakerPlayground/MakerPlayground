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

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceMonitor extends SplitPane implements SerialPortMessageListener {

    private static final Pattern format = Pattern.compile("(\\[\\[I]]|\\[\\[E]]|\\[\\[V]])?\\s\"(.*)\"\\s(.+)", Pattern.DOTALL); // Regex
    private static final Pattern numberRegex = Pattern.compile("^(-?\\d+\\.\\d+)$|^(-?\\d+)$");

    private SerialPort serialPort;
    private ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);

    private final ObservableList<LogItems> logData = FXCollections.observableArrayList();
//    private final ObservableList<XYChart.Data<Number, Number>> logChartData = FXCollections.observableArrayList();
    private final FilteredList<LogItems> logDataFilter = new FilteredList<>(logData);
    private final Map<String, LineChart<Number, Number>> lineChartMap = new HashMap<>();

    private final ObservableList<String> tagList = FXCollections.observableArrayList();

    @FXML private TableView<LogItems> deviceMonitorTable;
    @FXML private ComboBox<LogItems.LogLevel> levelComboBox;
    @FXML private CheckComboBox<String> checkTagComboBox;
    @FXML private CheckBox autoScrollCheckbox;
    @FXML private CheckComboBox<String> plotTagComboBox;
    @FXML private VBox chartPane;

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

        levelComboBox.getItems().addAll(FXCollections.observableArrayList(LogItems.LogLevel.values()));
        levelComboBox.getSelectionModel().select(0);
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(observable -> updateLogFilter());

        Bindings.bindContent(checkTagComboBox.getItems(), tagList);
        checkTagComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) observable -> updateLogFilter());

        updateLogFilter();

        // initialize table

        TableColumn<LogItems, String> deviceNameTableColumn = new TableColumn<>("Device Name");
        deviceNameTableColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDeviceName()));

        TableColumn<LogItems, String> messageTableColumn = new TableColumn<>("Message");
        messageTableColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getMessage()));

        deviceMonitorTable.getColumns().addAll(deviceNameTableColumn, messageTableColumn);
        deviceMonitorTable.resizeColumn(deviceNameTableColumn, 50);
        deviceMonitorTable.resizeColumn(messageTableColumn, 100);
        deviceMonitorTable.setItems(logDataFilter);

        // initialize plot

        Bindings.bindContent(plotTagComboBox.getItems(), tagList);

        plotTagComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) observable -> {
            ObservableList<String> tagSelected = plotTagComboBox.getCheckModel().getCheckedItems();

            Set<String> tagToBeRemoved = new HashSet<>(tagList);
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
    }

    private void updateLogFilter() {
        ObservableList<String> tagList = checkTagComboBox.getCheckModel().getCheckedItems();
        int selectedPriority = levelComboBox.getSelectionModel().getSelectedItem().getPriority();
        logDataFilter.setPredicate(logItems -> tagList.contains(logItems.getDeviceName())
                && logItems.getLevel().getPriority() >= selectedPriority);
    }

    private void createLineChart(String tag) {
        Map<String, XYChart.Series<Number, Number>> valueSeriesMap = new HashMap<>();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Samples");
        xAxis.setAutoRanging(true);
//        xAxis.setAnimated(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
//        yAxis.setAnimated(false);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(tag);
        lineChart.setAnimated(false);

        logData.addListener((ListChangeListener<LogItems>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new UnsupportedOperationException();
                } else if (c.wasUpdated()) {
                    throw new UnsupportedOperationException();
                } else {
                    for (LogItems removedItem : c.getRemoved()) {
                        throw new UnsupportedOperationException();
                    }
                    for (LogItems addedItem : c.getAddedSubList()) {
                        if (addedItem.getDeviceName().equals(tag)) {
                            // assume that the format is "value = 10.5"
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

    public boolean isInitilized() {
        return initialized.get();
    }

    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }

    public boolean initialize(SerialPort port) {
        serialPort = port;
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.addDataListener(this);
        if (!serialPort.openPort()) {
            return false;
        }
        initialized.set(true);
        return true;
    }

    public boolean closePort() {
        if (serialPort != null) {
            serialPort.removeDataListener();
            initialized.set(false);
            return serialPort.closePort();
        } else {
            return true;
        }
    }

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
        String message = new String(event.getReceivedData()).strip();
        System.out.println(message.strip());

        Matcher log = format.matcher(message);
        if (log.find() && log.groupCount() == 3) {
            LogItems logItems = new LogItems(log.group(1), log.group(2), log.group(3));
            Platform.runLater(() -> {
                logData.addAll(logItems);
                if (!tagList.contains(logItems.getDeviceName())) {
                    tagList.add(logItems.getDeviceName());
                    checkTagComboBox.getCheckModel().check(logItems.getDeviceName());
                }
                if (autoScrollCheckbox.isSelected()) {
                    deviceMonitorTable.scrollTo(logItems);
                }
            });
        }
    }

    public static class LogItems {
        enum LogLevel {
            INFO("[[I]]", 0),
            VALUE("[[V]]", 1),
            ERROR("[[E]]", 2);

            String levelTag;
            int priority;

            LogLevel(String levelTag, int priority) {
                this.levelTag = levelTag;
                this.priority = priority;
            }

            public int getPriority() {
                return priority;
            }

            static public LogLevel fromString(String levelTag) {
                for (LogLevel level: LogLevel.values()) {
                    if (level.levelTag.equals(levelTag.trim())) {
                        return level;
                    }
                }
                throw new IllegalStateException("Cannot find LogLevel of tag: " + levelTag);
            }
        }

        private final LogLevel level;
        private final String deviceName;
        private final String message;

        LogItems(String level, String tag, String message) {
            this.level = LogLevel.fromString(level);
            this.deviceName = tag;
            this.message = message;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getMessage() {
            return message;
        }
    }
}


//        messageTableColumn.setCellFactory(param -> new TableCell<>() {
//            private Text label;
//            @Override
//            protected void updateItem(String item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null || item.isEmpty()) {
//                    setGraphic(null);
//                    return;
//                }
//                System.out.println(item);
//                label = new Text(item);
//                label.setWrappingWidth(this.getWidth());
//                setGraphic(label);
//            }
//        });