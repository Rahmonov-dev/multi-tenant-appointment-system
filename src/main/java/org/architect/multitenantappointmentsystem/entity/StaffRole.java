package org.architect.multitenantappointmentsystem.entity;
public enum StaffRole {
    OWNER("Egasi", 100),
    ADMIN("Administrator", 80),
    MANAGER("Menejer", 60),
    STAFF("Xodim", 40);

    private final String displayName;
    private final int priority;

    StaffRole(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

    public boolean canManage(StaffRole other) {
        return this.priority > other.priority;
    }
}
