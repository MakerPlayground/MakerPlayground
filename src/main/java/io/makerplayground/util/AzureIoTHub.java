package io.makerplayground.util;

public class AzureIoTHub implements AzureResource {
    private final String name;

    public AzureIoTHub(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
