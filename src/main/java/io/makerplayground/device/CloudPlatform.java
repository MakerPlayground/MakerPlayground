package io.makerplayground.device;

import java.util.List;

public enum CloudPlatform {
    BLYNK("Blynk", List.of("Auth Key")), NETPIE("Netpie", List.of("App Id", "Key", "Secret", "Alias"));

    private final String displayName;
    private final List<String> parameter;

    CloudPlatform(String displayName, List<String> parameter) {
        this.displayName = displayName;
        this.parameter = parameter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getParameter() {
        return parameter;
    }
}
