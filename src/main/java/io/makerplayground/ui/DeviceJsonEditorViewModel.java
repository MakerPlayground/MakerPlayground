package io.makerplayground.ui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@JsonSerialize
class DeviceJsonEditorViewModel {

    @JsonIgnore
    private static final String USER_DEVICE_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "developer_devices";
    @JsonIgnore
    private final File deviceJsonFile;
    @JsonIgnore
    private final File assetFolder;

    class Function {
        Peripheral type;
        PinType pintype;
    }

    class Port {
        String name;
        DevicePortType type;
        DevicePortSubType sub_type;
        List<Function> function;
        double v_min;
        double v_max;
        double x;
        double y;
        double angle;
    }

    class Parameter {
        String name;
        Constraint constraint;
    }

    class Action {
        String name;
        List<Parameter> parameter;
        List<String> value;
        String count;
    }

    class Compatibility {
        String name;
        List<Action> action;
        List<Value> value;
    }

    class PlatformCompatibility {
        Platform platform;
        String classname;
        List<String> library_dependency;
    }

    class CloudPlatformCompatibility {
        CloudPlatform cloud_platform;
        String classname;
        List<String> library_dependency;
    }

    @JsonProperty
    private String deviceId;
    @JsonProperty
    private String brand;
    @JsonProperty
    private String model;
    @JsonProperty
    private String url;
    @JsonProperty
    private DeviceType deviceType;
    @JsonProperty
    private FormFactor formFactor;
    @JsonProperty
    private double width;
    @JsonProperty
    private double height;
    @JsonProperty
    private List<Property> property;
    @JsonProperty
    private List<Port> port;
    @JsonProperty
    private List<Peripheral> connectivity;
    @JsonProperty
    private List<Compatibility> compatibility;
    @JsonProperty
    private List<PlatformCompatibility> platforms;
    @JsonProperty
    private List<CloudPlatformCompatibility> support_cloudplatform;

    DeviceJsonEditorViewModel(String deviceId) {
        deviceJsonFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "device.json");
        this.deviceId = deviceId;
        try {
            if (!deviceJsonFile.exists() && !deviceJsonFile.createNewFile()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Cannot create device.json for Device ID: " + deviceId);
                alert.setContentText("Please remove the device folder and try again");
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Cannot create device.json for Device ID: " + deviceId);
            alert.setContentText("Please remove the device and try again");
            alert.showAndWait();
        }

        assetFolder = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "asset");
        if (!assetFolder.exists()) {
            try {
                FileUtils.forceMkdir(assetFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(deviceJsonFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
