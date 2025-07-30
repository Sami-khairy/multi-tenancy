package ma.khairy.t_enant.modules;

import ma.khairy.t_enant.config.TenantContext;
import ma.khairy.t_enant.exception.TenantNotFoundException;
import ma.khairy.t_enant.flyway.TenantValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/modules") // Cet endpoint doit être sécurisé
public class ModuleController {

    private static final Logger log = LoggerFactory.getLogger(ModuleController.class);
    private final ModuleMigrationService migrationService;
    private final TenantValidationService tenantValidationService; // Injectez le service


    public ModuleController(ModuleMigrationService migrationService, TenantValidationService tenantValidationService) {
        this.migrationService = migrationService;
        this.tenantValidationService = tenantValidationService;
    }

    @PostMapping("/{moduleName}/install")
    public ResponseEntity<String> installModule(@PathVariable String moduleName) {
        String tenantName = TenantContext.getCurrentTenant();
        log.info("Requête reçue pour installer le module '{}' pour le tenant '{}'", moduleName, tenantName);

        try {
            // --- VALIDATION PRÉALABLE ---
            // On vérifie que le tenant existe AVANT de faire quoi que ce soit d'autre.
            tenantValidationService.validateTenantExists(tenantName);

            // Si la validation passe, on continue avec l'installation.
            migrationService.installAdditionalModule(tenantName, moduleName);
            String message = "Module '" + moduleName + "' installé avec succès pour le tenant '" + tenantName + "'.";
            log.info(message);
            return ResponseEntity.ok(message);

        } catch (TenantNotFoundException e) {
            // On intercepte spécifiquement l'erreur de tenant non trouvé pour un retour clair.
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Catch-all pour les autres erreurs potentielles.
            String errorMessage = "Échec de l'installation du module '" + moduleName + "'. Erreur: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.status(500).body(errorMessage);
        }
    }
}