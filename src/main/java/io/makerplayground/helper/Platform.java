package io.makerplayground.helper;

/**
 *
 */
public enum Platform {
    MP_ARDUINO("atmega328pb", "Maker Playground's Kit"),
    ARDUINO("uno", "Arduino"), //, ARM, RPI_LINUX, RPI_WIN10, GROOVE_ARDUINO
    GROVE_ARDUINO("uno", "Grove for Arduino");

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
