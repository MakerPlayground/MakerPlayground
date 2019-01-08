package io.makerplayground.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.ui.dialog.generate.GenerateView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class LocalStorage {

    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "config.json";

    private final BooleanProperty developerToolEnabled;
    private final DoubleProperty deviceDiagramZoomLevel;

    private static LocalStorage instance = null;

    private LocalStorage(@JsonProperty("developerToolEnabled") boolean developerToolEnabled, @JsonProperty("deviceDiagramZoomLevel") double deviceDiagramZoomLevel) {
        this.developerToolEnabled = new SimpleBooleanProperty(developerToolEnabled);
        this.deviceDiagramZoomLevel = new SimpleDoubleProperty(deviceDiagramZoomLevel);

        this.developerToolEnabled.addListener((observable, oldValue, newValue) -> save());
        this.deviceDiagramZoomLevel.addListener((observable, oldValue, newValue) -> save());
    }

    public static LocalStorage getInstance() {
        if (instance == null) {
            File configFile = new File(CONFIG_FILE_PATH);
            if (!configFile.exists()) {
                instance = createDefaultLocalStorage();
                save();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    instance = mapper.readValue(configFile, LocalStorage.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    public BooleanProperty developerToolEnabledProperty() {
        return developerToolEnabled;
    }

    public boolean isDeveloperToolEnabled() {
        return developerToolEnabled.get();
    }

    public void setDeveloperToolEnabled(boolean developerToolEnabled) {
        this.developerToolEnabled.set(developerToolEnabled);
    }

    public DoubleProperty deviceDiagramZoomLevelProperty() {
        return deviceDiagramZoomLevel;
    }

    public double getDeviceDiagramZoomLevel() {
        return deviceDiagramZoomLevel.get();
    }

    public void setDeviceDiagramZoomLevel(double deviceDiagramZoomLevel) {
        this.deviceDiagramZoomLevel.set(deviceDiagramZoomLevel);
    }

    private static LocalStorage createDefaultLocalStorage() {
        return new LocalStorage(false, GenerateView.DEFAULT_ZOOM_SCALE);
    }

    private static void save() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (!configFile.exists()) {
                FileUtils.forceMkdirParent(configFile);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(configFile, instance);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
}
