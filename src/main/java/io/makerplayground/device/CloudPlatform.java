package io.makerplayground.device;

import java.util.List;

public enum CloudPlatform {
    BLYNK("Blynk", List.of("Auth Key", "Wifi's SSID", "Wifi's Password"), "MP_BLYNK"),
    NETPIE("Netpie", List.of("App Id", "Key", "Secret", "Alias", "Wifi's SSID", "Wifi's Password"), ""); // TODO: Add netpie

    private final String displayName;
    private final List<String> parameter;
    private final String libName;

    CloudPlatform(String displayName, List<String> parameter, String libName) {
        this.displayName = displayName;
        this.parameter = parameter;
        this.libName = libName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getParameter() {
        return parameter;
    }

    public String getLibName() {
        return libName;
    }
}
