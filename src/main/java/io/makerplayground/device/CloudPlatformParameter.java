package io.makerplayground.device;

import java.util.HashMap;
import java.util.Map;

public class CloudPlatformParameter {
    private final CloudPlatform platform;
    private final Map<String, String> params;

    public CloudPlatformParameter(CloudPlatform cloudPlatform) {
        this.platform = cloudPlatform;
        this.params = new HashMap<>();
    }

    public String getParameter(String name) {
        if (platform.getParameter().contains(name)) {
            return params.get(name);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setParameter(String name, String value) {
        if (platform.getParameter().contains(name)) {
            params.put(name, value);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
