package io.makerplayground.helper;

/**
 *
 */
public enum Platform {
    ARDUINO("uno", "Arduino"), //, ARM, RPI_LINUX, RPI_WIN10, GROOVE_ARDUINO
    GROVE_ARDUINO("uno", "Grove for Arduino"),
    MP_ARDUINO("elektor_uno_r4", "Maker Playground's Kit");

    private String platformioId;
    private String displayName;

    Platform(String platformioId, String displayName) {
        this.platformioId = platformioId;
        this.displayName = displayName;
    }

    public String getPlatformioId() {
        return platformioId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
