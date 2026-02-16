package org.architect.multitenantappointmentsystem.entity;

public enum UserStatus {
    ACTIVE("Faol"),
    INACTIVE("Nofaol"),
    BLOCKED("Bloklangan");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}