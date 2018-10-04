package io.makerplayground.ui.dialog;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import io.makerplayground.project.Project;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.stage.Window;
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
    private final Pattern format = Pattern.compile("(\\[\\[ERROR]]\\s)?\\[\\[(.*)]]\\s(.+)"); // Regex
    private FilteredList<LogItems> logDataFilter = new FilteredList<>(logData);
    @FXML private TableView<LogItems> deviceMonitorTable;
    @FXML private ComboBox<LogItems.LogLevel> levelComboBox;
    @FXML private CheckComboBox<String> checkTagComboBox;
    @FXML private Label levelLabel;
    @FXML private Label tagLabel;
    @FXML private GridPane gridPane;
    @FXML private CheckBox onStatus;
    public DeviceMonitor(Project project, SerialPort comPort) {
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
                                    logData.add(logItems);
                                    deviceMonitorTable.scrollTo(logItems);
                                }
                                if(!checkTagComboBox.getItems().contains(logItems.getDeviceName())){       // use deviceName from flash memory of serial port to generate device deviceName box
                                    List<Integer> checkedItem = checkTagComboBox.getCheckModel().getCheckedIndices();
                                    checkTagComboBox.getItems().add(logItems.getDeviceName());
                                    checkTagComboBox.getCheckModel().checkIndices(checkedItem.stream().mapToInt(value -> value).toArray());     // get the newest device check
                                    checkTagComboBox.getCheckModel().check(logItems.getDeviceName());
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
    private Optional<LogItems> getFormatLog(String rawLog) {
        Matcher log = format.matcher(rawLog);
        if (log.find()) {
            return Optional.of(new LogItems(log.group(1).trim(), log.group(2), log.group(3)));
        } else {
            System.out.println("Not match");
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
                    if (level.levelTag.equals(levelTag)) {
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