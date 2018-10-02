package io.makerplayground.device;

import java.util.List;

public enum CloudPlatform {
    BLYNK("Blynk", List.of("Auth Key", "Wifi's SSID", "Wifi's Password")), NETPIE("Netpie", List.of("App Id", "Key", "Secret", "Alias", "Wifi's SSID", "Wifi's Password"));

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
