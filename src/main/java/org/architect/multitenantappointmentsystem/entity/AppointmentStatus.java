package org.architect.multitenantappointmentsystem.entity;

public enum AppointmentStatus {
    PENDING("Kutilmoqda", "⏳"),
    CONFIRMED("Tasdiqlangan", "✅"),
    CANCELLED("Bekor qilingan", "❌"),
    COMPLETED("Yakunlangan", "✔️"),
    NO_SHOW("Kelmadi", "👻");

    private final String displayName;
    private final String icon;

    AppointmentStatus(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
}
