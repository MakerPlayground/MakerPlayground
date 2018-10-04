package io.makerplayground.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudPlatformLibrary {
    private final String className;
    private final List<String> dependency;

    public CloudPlatformLibrary(String className, List<String> dependency) {
        this.className = className;
        this.dependency = dependency;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getDependency() {
        return dependency;
    }
}
