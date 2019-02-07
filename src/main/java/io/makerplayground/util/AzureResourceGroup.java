package io.makerplayground.util;

public class AzureResourceGroup {
    private final String name;
    private final String location;

    public AzureResourceGroup(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
