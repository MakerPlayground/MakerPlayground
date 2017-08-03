package io.makerplayground.project;

import io.makerplayground.device.Device;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.Platform;

/**
 * Created by Palmn on 7/15/2017.
 */
public class ProjectController {
    private Platform platform;
    private Device controller;

    public ProjectController(Platform platform) {
        this.platform = platform;
        //this.controller = DeviceLibrary.INSTANCE.getActualDevice("DEV-13975");
        this.controller = DeviceLibrary.INSTANCE.getActualDevice("MCU_1");
    }

    ProjectController(Platform platform, Device controller) {
        this.platform = platform;
        //this.controller = DeviceLibrary.INSTANCE.getActualDevice("DEV-13975");;
        this.controller = DeviceLibrary.INSTANCE.getActualDevice("MCU_1");
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Device getController() {
        return controller;
    }

    public void setController(Device controller) {
        this.controller = controller;
    }
}
