package ma.khairy.t_enant.flyway;

import ma.khairy.t_enant.modules.ModuleMigrationService; // Importez le bon service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final ModuleMigrationService moduleMigrationService;
    private final TenantValidationService tenantValidationService;


    public TenantController(ModuleMigrationService moduleMigrationService, TenantValidationService tenantValidationService) {
        this.moduleMigrationService = moduleMigrationService;
        this.tenantValidationService = tenantValidationService;
    }

    @PostMapping("/{tenantName}")
    public ResponseEntity<String> createTenant(@PathVariable String tenantName) {
        // ÉTAPE 1 : Validation
        if (tenantValidationService.tenantExists(tenantName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Le tenant '" + tenantName + "' existe déjà.");
        }

        try {
            // ÉTAPE 2 : Provisioning du schéma
            moduleMigrationService.createTenantSchema(tenantName);

            // ÉTAPE 3 : Migration du module core
            moduleMigrationService.installCoreModule(tenantName);

            return ResponseEntity.ok("Tenant '" + tenantName + "' créé avec succès (module 'core' installé).");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la création du tenant: " + e.getMessage());
        }
    }
}