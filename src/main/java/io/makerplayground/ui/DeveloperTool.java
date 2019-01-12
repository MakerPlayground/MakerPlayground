package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DeveloperTool extends SplitPane {

    private static final String DEVICE_PATH = new File("").getAbsoluteFile().getPath() + File.separator + "library" + File.separator + "devices";
    private static final String LIB_PATH = new File("").getAbsoluteFile().getPath() + File.separator + "library" + File.separator + "lib" + File.separator + "arduino";
    private static final String LIB_EXT_PATH = new File("").getAbsoluteFile().getPath() + File.separator + "library" + File.separator + "lib_ext";
    private static final String USER_DEVICE_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "developer_devices";
    private static final String USER_LIB_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "developer_libraries";
    private static final String USER_LIB_EXT_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "developer_ext_libraries";

    @FXML TabPane rightPane;

    @FXML Button createLibButton;
    @FXML TextField deviceIdTextField;
    @FXML ListView<File> deviceListView;

    @FXML Button createDeviceButton;
    @FXML TextField libNameTextField;
    @FXML ListView<File> libListView;

    @FXML Button existingDeviceButton;
    @FXML Button existingLibButton;
    @FXML Button existingExtLibButton;

    @FXML Button userDeviceButton;
    @FXML Button userLibButton;
    @FXML Button userExtLibButton;

    private final ObservableList<File> devicePathList;
    private final ObservableList<File> libraryList;

    private final Map<File, Tab> openTabMap;

    public DeveloperTool() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeveloperTool.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File userDeviceDir = new File(USER_DEVICE_PATH);
        devicePathList = FXCollections.observableArrayList(userDeviceDir.listFiles(File::isDirectory));

        File userLibraryDir = new File(USER_LIB_PATH);
        libraryList = FXCollections.observableArrayList(userLibraryDir.listFiles(File::isDirectory));

        openTabMap = new HashMap<>();

        initEvents();
    }

    private void forceMakeDirAndOpen(String path) {
        try {
            File userDeviceDir = new File(path);
            if (!userDeviceDir.exists()) {
                FileUtils.forceMkdir(userDeviceDir);
            }
            Desktop.getDesktop().open(userDeviceDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initEvents() {
        existingDeviceButton.setOnAction(event -> forceMakeDirAndOpen(DEVICE_PATH));
        existingLibButton.setOnAction(event -> forceMakeDirAndOpen(LIB_PATH));
        existingExtLibButton.setOnAction(event -> forceMakeDirAndOpen(LIB_EXT_PATH));
        userDeviceButton.setOnAction(event -> forceMakeDirAndOpen(USER_DEVICE_PATH));
        userLibButton.setOnAction(event -> forceMakeDirAndOpen(USER_LIB_PATH));
        userExtLibButton.setOnAction(event -> forceMakeDirAndOpen(USER_LIB_EXT_PATH));

        createDeviceButton.setOnAction(event -> {
            String deviceId = deviceIdTextField.getText().trim();
            if (deviceId.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Please specify Device ID");
                alert.showAndWait();
                return;
            }

            if (DeviceLibrary.INSTANCE.getActualDevice().stream().anyMatch(actualDevice -> actualDevice.getId().equalsIgnoreCase(deviceId)) ) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("There exists the system device with ID: " + deviceId);
                alert.setContentText("Please change your device id");
                alert.showAndWait();
                return;
            }

            if (devicePathList.stream().anyMatch(file -> file.getName().equalsIgnoreCase(deviceId))) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("There exists a device with ID: " + deviceId);
                alert.setContentText("Please change your device id");
                alert.showAndWait();
                return;
            }

            File createdDevicePath = new File(USER_DEVICE_PATH + File.separator + deviceId);
            try {
                if (!createdDevicePath.exists()) {
                    FileUtils.forceMkdir(createdDevicePath);
                    devicePathList.add(createdDevicePath);
                    deviceIdTextField.setText("");
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Cannot create device with ID: " + deviceId);
                alert.setContentText("Please check if there is the illegal directory name");
                alert.showAndWait();
            }
        });

        deviceListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        deviceListView.itemsProperty().setValue(devicePathList);

        deviceListView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            File file = deviceListView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1 && openTabMap.containsKey(file)) {
                rightPane.getSelectionModel().select(openTabMap.get(file));
            } else if (event.getClickCount() == 2){
                if (openTabMap.containsKey(file)) {
                    rightPane.getSelectionModel().select(openTabMap.get(file));
                } else {
                    Tab newTab = new Tab();
                    newTab.setText("Device: " + file.getName());
                    newTab.setContent(new DeviceJsonEditorView(new DeviceJsonEditorViewModel(file.getName())));
                    rightPane.getTabs().add(newTab);
                    rightPane.getSelectionModel().select(newTab);
                    openTabMap.put(file, newTab);
                }
            }
        });

        createLibButton.setOnAction(event -> {
            String libName = libNameTextField.getText().trim();
            if (libName.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Please specify Library Name");
                alert.showAndWait();
                return;
            }

            if (DeviceLibrary.INSTANCE.getActualDevice().stream().anyMatch(actualDevice -> actualDevice.getMpLibrary().stream().anyMatch(s -> s.equalsIgnoreCase(libName)))) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("There exists the system library: " + libName);
                alert.setContentText("Please change your library name");
                alert.showAndWait();
                return;
            }

            if (libraryList.stream().anyMatch(file -> file.getName().equalsIgnoreCase(libName))) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("There exists a library: " + libName);
                alert.setContentText("Please change your library name");
                alert.showAndWait();
                return;
            }

            File createdLibraryPath = new File(USER_LIB_PATH + File.separator + libName);
            try {
                if (!createdLibraryPath.exists()) {
                    FileUtils.forceMkdir(createdLibraryPath);
                    libraryList.add(createdLibraryPath);
                    libNameTextField.setText("");
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Cannot create library: " + libName);
                alert.setContentText("Please check if there is the illegal directory name");
                alert.showAndWait();
            }

            File deviceJsonFile = new File(USER_LIB_PATH + File.separator + libName + File.separator + "library.json");
            try {
                if (!deviceJsonFile.exists() && !deviceJsonFile.createNewFile()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText("Cannot create library file for library: " + libName);
                    alert.setContentText("Please remove the library folder and try again");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Cannot create library file for library: " + libName);
                alert.setContentText("Please remove the library folder and try again");
                alert.showAndWait();
            }
        });

        libListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });

        libListView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            File file = libListView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1 && openTabMap.containsKey(file)) {
                rightPane.getSelectionModel().select(openTabMap.get(file));
            } else if (event.getClickCount() == 2){
                if (openTabMap.containsKey(file)) {
                    rightPane.getSelectionModel().select(openTabMap.get(file));
                } else {
                    Tab newTab = new Tab();
                    newTab.setText("Library: " + file.getName());
                    newTab.setContent(new DeviceJsonEditorView(new DeviceJsonEditorViewModel(file.getName())));
                    rightPane.getTabs().add(newTab);
                    rightPane.getSelectionModel().select(newTab);
                    openTabMap.put(file, newTab);
                }
            }
        });
        libListView.itemsProperty().setValue(libraryList);

        rightPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            while(c.next()) {
                if (c.wasRemoved()) {
                    for (Tab tab: c.getRemoved()) {
                        List<File> fileTabToRemove = openTabMap.entrySet().stream()
                                .filter(fileTabEntry -> fileTabEntry.getValue().equals(tab))
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                        for (File file: fileTabToRemove) {
                            openTabMap.remove(file);
                        }
                    }
                }
            }
        });
    }
}
