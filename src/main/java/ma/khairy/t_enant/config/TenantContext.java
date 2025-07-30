package ma.khairy.t_enant.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    // Cette constante n'est plus un fallback logique, mais un point de réinitialisation technique
    // pour les connexions JDBC. Le schéma "public" existe toujours dans PostgreSQL.
    public static final String RESET_TENANT_ID = "public";

    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting tenant to {}", tenantId);
        currentTenant.set(tenantId);
    }

    /**
     * Récupère l'ID du tenant actuel depuis le ThreadLocal.
     * Ne retourne plus de valeur par défaut.
     * Renvoie null si aucun tenant n'est défini.
     * @return L'ID du tenant, ou null.
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        log.debug("Clearing tenant context");
        currentTenant.remove();
    }
}