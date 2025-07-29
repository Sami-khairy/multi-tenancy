package ma.khairy.t_enant.security.keycloak;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class KeycloakAdminController {

    private final KeycloakUserService keycloakUserService;

    public KeycloakAdminController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    /**
     * Endpoint pour rechercher un utilisateur dans le realm du tenant actuel.
     * @param username Le nom de l'utilisateur à rechercher.
     * @return Les détails de l'utilisateur s'il est trouvé.
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<UserRepresentation> findUser(@PathVariable String username) {
        UserRepresentation user = keycloakUserService.findUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}