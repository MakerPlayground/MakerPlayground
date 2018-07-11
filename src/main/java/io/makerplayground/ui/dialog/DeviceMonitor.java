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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Window;
import org.controlsfx.control.CheckComboBox;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class DeviceMonitor extends Dialog implements InvalidationListener{

    private TableView<LogItems> deviceMonitorTable = new TableView<>();
    private ObservableList<LogItems> logData = FXCollections.observableArrayList();
    private Thread serialThread = null;
    private final Pattern format = Pattern.compile("(DEBUG|VERBOSE|WARNING|ERROR|INFO);(.+);(.+)"); // Regex
    private FilteredList<LogItems> logDataFilter = new FilteredList<>(logData);
    private final ComboBox<LogItems.LogLevel> levelComboBox;
    private CheckComboBox<String> checkTagComboBox;

    public DeviceMonitor(Project project) {
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        setTitle("Device Monitor");


        Label levelLabel = new Label("Level");
        Label tagLabel = new Label("Device Tag");
        levelLabel.setMinWidth(30);
        tagLabel.setMinWidth(70);

        CheckBox onStatus = new CheckBox("On");
        onStatus.setSelected(true);


        checkTagComboBox = new CheckComboBox<>(FXCollections.observableArrayList(new ArrayList<>()));
        levelComboBox = new ComboBox<>(FXCollections.observableArrayList(LogItems.LogLevel.values()));
        checkTagComboBox.getCheckModel().checkAll();
        levelComboBox.setMinWidth(100);
        checkTagComboBox.setMaxWidth(300);
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(this);
        checkTagComboBox.getCheckModel().getCheckedItems().addListener(this);
        levelComboBox.getSelectionModel().select(0);


        TableColumn<LogItems, String> tagCol = new TableColumn<>("Device Tag");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("Tag"));
        tagCol.setMinWidth(200);
        TableColumn<LogItems, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("Message"));
        messageCol.setMinWidth(400);

        deviceMonitorTable.setItems(logDataFilter);
        deviceMonitorTable.getColumns().addAll(tagCol, messageCol);

        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(600,450);
        gridPane.setHgap(4);
        gridPane.setVgap(2);
        gridPane.setPadding(new Insets(5,5,5,5));
        gridPane.add(levelLabel,5,2);
        gridPane.add(levelComboBox,10,2);
        gridPane.add(tagLabel,19,2);
        gridPane.add(checkTagComboBox,24,2);
        gridPane.add(deviceMonitorTable,2,6,30,30);
        gridPane.add(onStatus,4,47);

        this.getDialogPane().setMinSize(100,100);
        this.getDialogPane().setContent(gridPane);

        initView();

        // Create thread to read data from serial port
        serialThread = new Thread(() -> {
            SerialPort comPort = SerialPort.getCommPorts()[0];
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
                        while(sb.indexOf("\n") >= 0) {
                            int index = sb.indexOf("\n");
                            String msg = sb.subSequence(0, index).toString();
                            sb.delete(0, index + 1);
                            System.out.println("msg: " +msg);
                            System.out.println(",remain: " + sb.toString());
                            getFormatLog(msg).ifPresent(logItems -> Platform.runLater(() -> {
                                if (onStatus.isSelected()) {
                                    logData.add(logItems);
                                    deviceMonitorTable.scrollTo(logItems);
                                }
                                if(!checkTagComboBox.getItems().contains(logItems.getTag())){       // use tag from flash memory of serial port to generate device tag box
                                    List<Integer> checkedItem = checkTagComboBox.getCheckModel().getCheckedIndices();
                                    checkTagComboBox.getItems().add(logItems.getTag());
                                    checkTagComboBox.getCheckModel().checkIndices(checkedItem.stream().mapToInt(value -> value).toArray());     // get the newest device check
                                    checkTagComboBox.getCheckModel().check(logItems.getTag());
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
    public Optional<LogItems> getFormatLog(String rawLog) {
        Matcher log = format.matcher(rawLog);
        if (log.find()) {
            return Optional.of(new LogItems(log.group(1),log.group(2),log.group(3)));
        } else {
            return Optional.empty();
        }
    }

    // Change display data in table from value of combobox
    @Override
    public void invalidated(Observable observable) {
        logDataFilter.setPredicate(logItems -> {
            if (levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.VERBOSE)) {
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.VERBOSE)
                        || logItems.getLevel().equals(LogItems.LogLevel.INFO)
                        || logItems.getLevel().equals(LogItems.LogLevel.DEBUG)
                        || logItems.getLevel().equals(LogItems.LogLevel.WARNING)
                        || logItems.getLevel().equals(LogItems.LogLevel.ERROR)

                );
            }
            else if(levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.DEBUG)){
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.INFO)
                        || logItems.getLevel().equals(LogItems.LogLevel.DEBUG)
                        || logItems.getLevel().equals(LogItems.LogLevel.WARNING)
                        || logItems.getLevel().equals(LogItems.LogLevel.ERROR)
                );
            }
            else if(levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.INFO)) {
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.INFO)
                        || logItems.getLevel().equals(LogItems.LogLevel.WARNING)
                        || logItems.getLevel().equals(LogItems.LogLevel.ERROR)
                );

            }
            else if(levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.WARNING)){
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.WARNING)
                        || logItems.getLevel().equals(LogItems.LogLevel.ERROR)
                );
            }
            else
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && logItems.getLevel().equals(LogItems.LogLevel.ERROR);
        });
    }

    public static class LogItems {
        enum LogLevel {VERBOSE,DEBUG,INFO,WARNING,ERROR}

        private final LogLevel level;
        private final String tag;
        private final String message;

        public LogItems(String level, String tag, String message) {
            this.level = LogLevel.valueOf(level);
            this.tag = tag;
            this.message = message;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }


    }
}