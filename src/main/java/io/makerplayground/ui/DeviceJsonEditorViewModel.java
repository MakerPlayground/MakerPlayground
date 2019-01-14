package io.makerplayground.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@JsonSerialize
class DeviceJsonEditorViewModel {

    @JsonIgnore
    private static final String USER_DEVICE_PATH = System.getProperty("user.home") + File.separator + ".makerplayground" + File.separator + "developer_devices";
    @JsonIgnore
    private final File devicePngFile;
    @JsonIgnore
    private final File deviceSvgFile;
    @JsonIgnore
    private final File devicePngBakFile;
    @JsonIgnore
    private final File deviceSvgBakFile;
    @JsonIgnore
    private final File deviceJsonFile;
    @JsonIgnore
    private final File assetFolder;

    class Function {
        Peripheral type;
        PinType pintype;

        @JsonCreator
        public Function(@JsonProperty("type") Peripheral type,
                        @JsonProperty("pintype") PinType pintype) {
            this.type = type;
            this.pintype = pintype;
        }
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

        @JsonCreator
        public Port(@JsonProperty("name") String name,
                    @JsonProperty("type") DevicePortType type,
                    @JsonProperty("sub_type") DevicePortSubType sub_type,
                    @JsonProperty("function") List<Function> function,
                    @JsonProperty("v_min") double v_min,
                    @JsonProperty("v_max") double v_max,
                    @JsonProperty("x") double x,
                    @JsonProperty("y") double y,
                    @JsonProperty("angle") double angle) {
            this.name = name;
            this.type = type;
            this.sub_type = sub_type;
            this.function = function;
            this.v_min = v_min;
            this.v_max = v_max;
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }

    class Parameter {
        String name;
        Constraint constraint;

        @JsonCreator
        public Parameter(@JsonProperty("name") String name,
                         @JsonProperty("constraint") Constraint constraint) {
            this.name = name;
            this.constraint = constraint;
        }
    }

    class Action {
        String name;
        List<Parameter> parameter;
        List<String> value;
        String count;

        @JsonCreator
        public Action(@JsonProperty("name") String name,
                      @JsonProperty("parameter") List<Parameter> parameter,
                      @JsonProperty("value") List<String> value,
                      @JsonProperty("count") String count) {
            this.name = name;
            this.parameter = parameter;
            this.value = value;
            this.count = count;
        }
    }

    class Compatibility {
        String name;
        List<Action> action;
        List<Value> value;

        @JsonCreator
        public Compatibility(@JsonProperty("name") String name,
                             @JsonProperty("action") List<Action> action,
                             @JsonProperty("value") List<Value> value) {
            this.name = name;
            this.action = action;
            this.value = value;
        }
    }

    class PlatformCompatibility {
        Platform platform;
        String classname;
        List<String> library_dependency;

        @JsonCreator
        public PlatformCompatibility(@JsonProperty("platform") Platform platform,
                                     @JsonProperty("classname") String classname,
                                     @JsonProperty("library_dependency") List<String> library_dependency) {
            this.platform = platform;
            this.classname = classname;
            this.library_dependency = library_dependency;
        }
    }

    class CloudPlatformCompatibility {
        CloudPlatform cloud_platform;
        String classname;
        List<String> library_dependency;

        @JsonCreator
        public CloudPlatformCompatibility(@JsonProperty("cloud_platform") CloudPlatform cloud_platform,
                                          @JsonProperty("classname") String classname,
                                          @JsonProperty("library_dependency") List<String> library_dependency) {
            this.cloud_platform = cloud_platform;
            this.classname = classname;
            this.library_dependency = library_dependency;
        }
    }

    private String deviceId;
    private String brand;
    private String model;
    private String url;
    private DeviceType deviceType;
    private FormFactor formFactor;
    private double width;
    private double height;
    private List<Property> property;
    private List<Port> port;
    private List<Peripheral> connectivity;
    private List<Compatibility> compatibility;
    private List<PlatformCompatibility> platforms;
    private List<CloudPlatformCompatibility> support_cloudplatform;

    DeviceJsonEditorViewModel(String deviceId) {
        this.deviceId = deviceId;
        this.deviceJsonFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "device.json");
        this.devicePngFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "/asset/device.png");
        this.deviceSvgFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "/asset/device.svg");
        this.devicePngBakFile = new File(FileUtils.getTempDirectoryPath() + File.separator + deviceId + File.separator + "/asset/device_bak.png");
        this.deviceSvgBakFile = new File(FileUtils.getTempDirectoryPath() + File.separator + deviceId + File.separator + "/asset/device_bak.svg");
        if (!deviceJsonFile.exists()) {
            setVariableDefaultValues();
            save();
        }
        load();

        assetFolder = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "asset");
        if (!assetFolder.exists()) {
            try {
                FileUtils.forceMkdir(assetFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.backupImages();
    }

    @JsonCreator
    public DeviceJsonEditorViewModel(@JsonProperty("deviceId") String deviceId,
                                     @JsonProperty("brand") String brand,
                                     @JsonProperty("model") String model,
                                     @JsonProperty("url") String url,
                                     @JsonProperty("deviceType") DeviceType deviceType,
                                     @JsonProperty("formFactor") FormFactor formFactor,
                                     @JsonProperty("width") double width,
                                     @JsonProperty("height") double height,
                                     @JsonProperty("property") List<Property> property,
                                     @JsonProperty("port") List<Port> port,
                                     @JsonProperty("connectivity") List<Peripheral> connectivity,
                                     @JsonProperty("compatibility") List<Compatibility> compatibility,
                                     @JsonProperty("platforms") List<PlatformCompatibility> platforms,
                                     @JsonProperty("support_cloudplatform") List<CloudPlatformCompatibility> support_cloudplatform)
    {
        this.deviceId = deviceId;
        this.brand = brand;
        this.model = model;
        this.url = url;
        this.deviceType = deviceType;
        this.formFactor = formFactor;
        this.width = width;
        this.height = height;
        this.property = property;
        this.port = port;
        this.connectivity = connectivity;
        this.compatibility = compatibility;
        this.platforms = platforms;
        this.support_cloudplatform = support_cloudplatform;

        this.deviceJsonFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "device.json");
        this.assetFolder = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "asset");
        this.devicePngFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "/asset/device.png");
        this.deviceSvgFile = new File(USER_DEVICE_PATH + File.separator + deviceId + File.separator + "/asset/device.svg");
        this.devicePngBakFile = new File(FileUtils.getTempDirectoryPath() + File.separator + deviceId + File.separator + "/asset/device_bak.png");
        this.deviceSvgBakFile = new File(FileUtils.getTempDirectoryPath() + File.separator + deviceId + File.separator + "/asset/device_bak.svg");

        this.backupImages();
    }

    private void setVariableDefaultValues() {
        this.brand = "";
        this.model = "";
        this.url = "";
        this.deviceType = DeviceType.PERIPHERAL;
        this.formFactor = FormFactor.BREAKOUT_BOARD_ONESIDE;
        this.width = 0.0;
        this.height = 0.0;
        this.property = Collections.emptyList();
        this.port = Collections.emptyList();
        this.connectivity = Collections.emptyList();
        this.compatibility = Collections.emptyList();
        this.platforms = Collections.emptyList();
        this.support_cloudplatform = Collections.emptyList();
    }

    void saveDevicePng(File file){
        forceSaveFile(file, devicePngFile);
        Image image = new Image(file.toURI().toString());
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    void deleteDevicePng() {
        forceDeleteFile(devicePngFile);
        this.width = 0.0;
        this.height = 0.0;
    }

    void saveDeviceSvg(File file) {
        forceSaveFile(file, deviceSvgFile);
    }

    void deleteDeviceSvg() {
        forceDeleteFile(deviceSvgFile);
    }

    private void forceSaveFile(File file, File target) {
        try {
            if (!target.exists()) {
                FileUtils.forceMkdirParent(target);
            }
            FileUtils.copyFile(file, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forceDeleteFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!deviceJsonFile.exists() && !deviceJsonFile.createNewFile()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot create device.json for Device ID: " + deviceId);
                alert.setContentText("Please remove the device folder and try again");
                alert.showAndWait();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(deviceJsonFile, this);
                this.removeBackupImages();
                this.backupImages();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void discard() {
        this.restoreBackupImages();
        this.load();
    }

    private void backupImages() {
        try {
            removeBackupImages();
            if (devicePngFile.exists()) {
                FileUtils.copyFile(devicePngFile, devicePngBakFile);
            }
            if (deviceSvgFile.exists()) {
                FileUtils.copyFile(deviceSvgFile, deviceSvgBakFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeBackupImages() {
        try {
            if (devicePngBakFile.exists()) {
                FileUtils.forceDelete(devicePngBakFile);
            }
            if (deviceSvgBakFile.exists()) {
                FileUtils.forceDelete(deviceSvgBakFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreBackupImages() {
        try {
            if (devicePngFile.exists()) {
                FileUtils.forceDelete(devicePngFile);
            }
            if (deviceSvgFile.exists()) {
                FileUtils.forceDelete(deviceSvgFile);
            }
            if (devicePngBakFile.exists()) {
                FileUtils.copyFile(devicePngBakFile, devicePngFile);
            }
            if (deviceSvgBakFile.exists()) {
                FileUtils.copyFile(deviceSvgBakFile, deviceSvgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DeviceJsonEditorViewModel obj = mapper.readValue(deviceJsonFile, DeviceJsonEditorViewModel.class);
            this.deviceId = obj.deviceId;
            this.brand = obj.brand;
            this.model = obj.model;
            this.url = obj.url;
            this.deviceType = obj.deviceType;
            this.formFactor = obj.formFactor;
            this.width = obj.width;
            this.height = obj.height;
            this.property = obj.property;
            this.port = obj.port;
            this.connectivity = obj.connectivity;
            this.compatibility = obj.compatibility;
            this.platforms = obj.platforms;
            this.support_cloudplatform = obj.support_cloudplatform;
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot load device.json for Device ID: " + deviceId);
            alert.setContentText("Please check the device.json file and try again");
            alert.showAndWait();
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getUrl() {
        return url;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public FormFactor getFormFactor() {
        return formFactor;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public List<Property> getProperty() {
        return property;
    }

    public List<Port> getPort() {
        return port;
    }

    public List<Peripheral> getConnectivity() {
        return connectivity;
    }

    public List<Compatibility> getCompatibility() {
        return compatibility;
    }

    public List<PlatformCompatibility> getPlatforms() {
        return platforms;
    }

    public List<CloudPlatformCompatibility> getSupport_cloudplatform() {
        return support_cloudplatform;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void setFormFactor(FormFactor formFactor) {
        this.formFactor = formFactor;
    }

    public void setProperty(List<Property> property) {
        this.property = property;
    }

    public void setPort(List<Port> port) {
        this.port = port;
    }

    public void setConnectivity(List<Peripheral> connectivity) {
        this.connectivity = connectivity;
    }

    public void setCompatibility(List<Compatibility> compatibility) {
        this.compatibility = compatibility;
    }

    public void setPlatforms(List<PlatformCompatibility> platforms) {
        this.platforms = platforms;
    }

    public void setSupport_cloudplatform(List<CloudPlatformCompatibility> support_cloudplatform) {
        this.support_cloudplatform = support_cloudplatform;
    }

    @JsonIgnore
    public Image getPngImage() {
        return this.devicePngFile.exists() ? new Image(this.devicePngFile.toURI().toString()) : null;
    }

    @JsonIgnore
    public File getSvgImage() {
        return this.deviceSvgFile;
    }
}
