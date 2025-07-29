package ma.khairy.t_enant.security.keycloak;

import ma.khairy.t_enant.exception.TenantNotFoundException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KeycloakAdminClientManager {

    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;

    private final TenantAdminClientProperties properties;

    // Cache pour éviter de recréer le client à chaque appel
    private final Map<String, Keycloak> adminClients = new ConcurrentHashMap<>();

    public KeycloakAdminClientManager(TenantAdminClientProperties properties) {
        this.properties = properties;
    }

    /**
     * Récupère ou crée une instance du client d'administration Keycloak pour un tenant donné.
     * @param realm Le nom du realm (tenantId)
     * @return Une instance de Keycloak configurée.
     */
    public Keycloak getInstance(String realm) {
        // Utilise computeIfAbsent pour une création et mise en cache thread-safe
        return adminClients.computeIfAbsent(realm, this::buildInstance);
    }

    private Keycloak buildInstance(String realm) {
        TenantAdminClientProperties.AdminClientConfig config = properties.getAdminClients().get(realm);

        if (config == null) {
            throw new TenantNotFoundException("Aucune configuration de client d'administration trouvée pour le tenant: " + realm);
        }

        return KeycloakBuilder.builder()
                .serverUrl(keycloakBaseUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .build();
    }
}