package io.makerplayground.util;

public class AzureIoTHubDevice implements AzureResource {
    private final String deviceId;
    private String connectionString;

    public AzureIoTHubDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    public AzureIoTHubDevice(String deviceId, String connectionString) {
        this.deviceId = deviceId;
        this.connectionString = connectionString;
    }

    @Override
    public String getName() {
        return deviceId;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
