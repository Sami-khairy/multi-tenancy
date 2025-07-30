package ma.khairy.t_enant.flyway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantMigrationService tenantMigrationService;

    public TenantController(TenantMigrationService tenantMigrationService) {
        this.tenantMigrationService = tenantMigrationService;
    }

    @PostMapping("/{tenantName}")
    public ResponseEntity<String> createTenant(@PathVariable String tenantName) {
        try {
            tenantMigrationService.provisionTenant(tenantName);
            return ResponseEntity.ok("Tenant '" + tenantName + "' créé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la création du tenant: " + e.getMessage());
        }
    }
}