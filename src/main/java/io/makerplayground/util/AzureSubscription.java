package io.makerplayground.util;

public class AzureSubscription {
    private final String name;
    private final String id;
    private final String tenantId;
    private final String userName;

    public AzureSubscription(String name, String id, String tenantId, String userName) {
        this.name = name;
        this.id = id;
        this.tenantId = tenantId;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserName() {
        return userName;
    }
}
