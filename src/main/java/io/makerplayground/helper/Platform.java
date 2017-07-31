package io.makerplayground.helper;

/**
 *
 */
public enum Platform {
    ARDUINO("uno");//, ARM, RPI_LINUX, RPI_WIN10, GROOVE_ARDUINO

    private String platformioId;

    Platform(String platformioId) {
        this.platformioId = platformioId;
    }

    public String getPlatformioId() {
        return platformioId;
    }
}
