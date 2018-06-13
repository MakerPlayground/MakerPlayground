package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.generator.Diagram;

import io.makerplayground.generator.MPDiagram;
import io.makerplayground.helper.SingletonWiringDiagram;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import io.makerplayground.ui.devicepanel.TableDataList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.Window;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
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

        this.getDialogPane().setMinSize(100,100);
        this.getDialogPane().setContent(gridPane);

        initView();

        // Create thread to read data from serial port
        serialThread = new Thread(() -> {
                SerialPort comPort = SerialPort.getCommPorts()[0];
                comPort.openPort();
                comPort.setBaudRate(115200);
                System.out.println(SerialPort.getCommPorts()[0]);
                comPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 0);
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(comPort.getInputStream()))) {
                    String s = null;
                    while (!serialThread.isInterrupted() && (s = bufferedReader.readLine()) != null) {  //get raw data and convert to format data
                        getFormatLog(s).ifPresent(logItems -> Platform.runLater(() -> {
                            logData.add(logItems);
                            if(!checkTagComboBox.getItems().contains(logItems.getTag())){       // use tag from flash memory of serial port to generate device tag box
                                List<Integer> checkedItem = checkTagComboBox.getCheckModel().getCheckedIndices();
                                checkTagComboBox.getItems().add(logItems.getTag());
                                checkTagComboBox.getCheckModel().checkIndices(checkedItem.stream().mapToInt(value -> value).toArray());     // get the newest device check
                                checkTagComboBox.getCheckModel().check(logItems.getTag());
                            }
                        }));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                comPort.closePort();

        });
        serialThread.start();
    }

    private void initView() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            window.hide();
            serialThread.interrupt();
        });
    }

    // Regex Function
    public Optional<LogItems> getFormatLog(String rawLog) {
        Matcher log = format.matcher(rawLog);
        if (log.find()) {
            //System.out.println("Found value: " + log.group(1));
            //System.out.println("Found value: " + log.group(2));
            //System.out.println("Found value: " + log.group(3));
            return Optional.of(new LogItems(log.group(1),log.group(2),log.group(3)));
        } else {
            //System.out.println("NO MATCH");
            return Optional.empty();
        }
    }

    // Change display data in table from value of combobox
    @Override
    public void invalidated(Observable observable) {
        //logDataFilter.setPredicate(logItems -> checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
        //&& LevelComboBox.getSelectionModel().getSelectedItem().contains(logItems.getLevel()));
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
            else if(levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.INFO)){
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.INFO)
                        || logItems.getLevel().equals(LogItems.LogLevel.DEBUG)
                        || logItems.getLevel().equals(LogItems.LogLevel.WARNING)
                        || logItems.getLevel().equals(LogItems.LogLevel.ERROR)
                );
            }
            else if(levelComboBox.getSelectionModel().getSelectedItem().equals(LogItems.LogLevel.DEBUG)) {
                return checkTagComboBox.getCheckModel().getCheckedItems().contains(logItems.getTag())
                        && (logItems.getLevel().equals(LogItems.LogLevel.DEBUG)
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
}