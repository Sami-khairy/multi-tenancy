package ma.khairy.t_enant.security.keycloak;

import ma.khairy.t_enant.config.TenantContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService {

    private final KeycloakAdminClientManager keycloakAdminClientManager;

    public KeycloakUserService(KeycloakAdminClientManager keycloakAdminClientManager) {
        this.keycloakAdminClientManager = keycloakAdminClientManager;
    }

    public UserRepresentation findUserByUsername(String username) {
        // 1. Obtenir le tenant de la requête actuelle
        String currentTenant = TenantContext.getCurrentTenant();

        // 2. Obtenir l'instance du client admin pour ce tenant
        Keycloak keycloak = keycloakAdminClientManager.getInstance(currentTenant);

        // 3. Utiliser le client pour interagir avec l'API d'administration de Keycloak
        return keycloak.realm(currentTenant)
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElse(null);
    }
}