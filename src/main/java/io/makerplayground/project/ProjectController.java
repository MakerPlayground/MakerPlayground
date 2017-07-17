package io.makerplayground.project;

import io.makerplayground.device.Device;
import io.makerplayground.helper.Platform;

/**
 * Created by Palmn on 7/15/2017.
 */
public class ProjectController {
    private Platform platform;
    private Device controller;

    public ProjectController(Platform platform) {
        this.platform = platform;
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
