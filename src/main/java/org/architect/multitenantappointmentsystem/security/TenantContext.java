package org.architect.multitenantappointmentsystem.security;

public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        Long tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set!");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
