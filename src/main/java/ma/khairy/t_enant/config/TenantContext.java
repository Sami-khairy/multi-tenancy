package ma.khairy.t_enant.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public static final String DEFAULT_TENANT_ID = "public"; // Un tenant par défaut

    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting tenant to {}", tenantId);
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        return tenant != null ? tenant : DEFAULT_TENANT_ID;
    }

    public static void clear() {
        log.debug("Clearing tenant context");
        currentTenant.remove();
    }
}