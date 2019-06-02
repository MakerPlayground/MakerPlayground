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

package io.makerplayground.ui.dialog;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class DeviceMonitor extends Dialog implements InvalidationListener{

    private ObservableList<LogItems> logData = FXCollections.observableArrayList();
    private Thread serialThread = null;
    private final Pattern format = Pattern.compile("(\\[\\[ERROR]]\\s)?\\[\\[(.*)]]\\s(.+)", Pattern.DOTALL); // Regex
    private FilteredList<LogItems> logDataFilter = new FilteredList<>(logData);
    @FXML private TableView<LogItems> deviceMonitorTable;
    @FXML private ComboBox<LogItems.LogLevel> levelComboBox;
    @FXML private CheckComboBox<String> checkTagComboBox;
    @FXML private Label levelLabel;
    @FXML private Label tagLabel;
    @FXML private GridPane gridPane;
    @FXML private CheckBox onStatus;

    @FXML private TableColumn<LogItems, String> messageTableColumn;

    public DeviceMonitor(SerialPort comPort) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/DeviceMonitor.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        setTitle("Device Monitor - " + comPort.getSystemPortName());


        checkTagComboBox.getItems().addAll(FXCollections.observableArrayList(new ArrayList<>()));
        levelComboBox.getItems().addAll(FXCollections.observableArrayList(LogItems.LogLevel.values()));

        checkTagComboBox.getCheckModel().checkAll();
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(this);

        checkTagComboBox.getCheckModel().getCheckedItems().addListener(this);
        levelComboBox.getSelectionModel().select(0);

        messageTableColumn.setCellFactory(param -> new TableCell<>() {
            private Text label;
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }
                System.out.println(item);
                label = new Text(item);
                label.setWrappingWidth(this.getWidth());
                setGraphic(label);
            }
        });
        deviceMonitorTable.setItems(logDataFilter);

        initView();

        // Create thread to read data from serial port
        serialThread = new Thread(() -> {
            comPort.openPort();
            comPort.setBaudRate(115200);
            while(!serialThread.isInterrupted()) {
                StringBuilder sb = new StringBuilder();
                comPort.addDataListener(new SerialPortDataListener() {
                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }

                    @Override
                    public void serialEvent(SerialPortEvent serialPortEvent) {
                        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                            return;
                        byte[] newData = new byte[comPort.bytesAvailable()];
                        comPort.readBytes(newData, newData.length);
                        sb.append(new String(newData));
                        while(sb.indexOf("\0") >= 0) {
                            int index = sb.indexOf("\0");
                            String msg = sb.subSequence(0, index).toString();
                            sb.delete(0, index + 1);
                            getFormatLog(msg).ifPresent(logItems -> Platform.runLater(() -> {
                                if (onStatus.isSelected()) {
                                    logData.addAll(logItems);
                                    deviceMonitorTable.scrollTo(logItems.get(logItems.size()-1));
                                }
                                for (LogItems item: logItems) {
                                    if(!checkTagComboBox.getItems().contains(item.getDeviceName())){       // use deviceName from flash memory of serial port to generate device deviceName box
                                        List<Integer> checkedItem = checkTagComboBox.getCheckModel().getCheckedIndices();
                                        checkTagComboBox.getItems().add(item.getDeviceName());
                                        checkTagComboBox.getCheckModel().checkIndices(checkedItem.stream().mapToInt(value -> value).toArray());     // get the newest device check
                                        checkTagComboBox.getCheckModel().check(item.getDeviceName());
                                    }
                                }
                            }));
                        }
                    }
                });
            }
            comPort.closePort();
        });
        serialThread.start();
    }

    private void initView() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            serialThread.interrupt();
            try {
                serialThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            window.hide();
        });
    }

    // Regex Function
    private Optional<List<LogItems>> getFormatLog(String rawLog) {
        System.out.println(rawLog);
        List<LogItems> logitems = new ArrayList<>();
        Matcher log = format.matcher(rawLog);
        while (log.find()) {
            System.out.println(log.group(1) + "____" + log.group(2) + "____" + log.group(3));
            logitems.add(new LogItems(log.group(1), log.group(2), log.group(3)));
        }
        if (logitems.size() > 0) {
            return Optional.of(logitems);
        }
        else {
            return Optional.empty();
        }
    }

    // Change display data in table from value of combobox
    @Override
    public void invalidated(Observable observable) {
        logDataFilter.setPredicate(
                logItems -> checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getDeviceName())
                && logItems.getLevel().getPriority() >= levelComboBox.getSelectionModel().getSelectedItem().getPriority()
        );
    }

    public static class LogItems {
        enum LogLevel {
            INFO("[[INFO]]", 0),
            ERROR("[[ERROR]]", 1);

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
            if (level == null) {
                level = "[[INFO]]";
            }
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