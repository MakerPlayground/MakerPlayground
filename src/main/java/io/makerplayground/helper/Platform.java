package io.makerplayground.helper;

/**
 *
 */
public enum Platform {
    MP_ARDUINO("Maker Playground's Kit", "arduino"),
    ARDUINO("Arduino", "arduino"),
    GROVE_ARDUINO("Grove for Arduino", "arduino"),
    ESP32("ESP32", "arduino");

    private String displayName;
    private String libFolderName;

    Platform(String displayName, String libFolderName) {
        this.displayName = displayName;
        this.libFolderName = libFolderName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLibraryFolderName() {
        return libFolderName;
    }
}
