package ma.khairy.t_enant.modules;

import ma.khairy.t_enant.config.TenantContext;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ModuleMigrationService.class);
    private final DataSource dataSource;
    private final InstalledModuleRepository installedModuleRepository;

    public ModuleMigrationService(DataSource dataSource, InstalledModuleRepository installedModuleRepository) {
        this.dataSource = dataSource;
        this.installedModuleRepository = installedModuleRepository;
    }

    /**
     * Installe le module "core" pour un tenant.
     * Cette méthode suppose que le schéma existe déjà.
     */
    @Transactional
    public void installCoreModule(String schemaName) {
        log.info("Lancement de la migration 'core' pour le schéma '{}'...", schemaName);
        Flyway flyway = Flyway.configure()
                .dataSource(this.dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/core")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();

        if (installedModuleRepository.findById("core").isEmpty()) {
            installedModuleRepository.save(new InstalledModule("core", true));
            log.info("Module 'core' enregistré pour le tenant '{}'.", schemaName);
        }
    }

    /**
     * Crée uniquement le schéma de base de données pour un tenant.
     */
    public void createTenantSchema(String schemaName) {
        log.info("Création du schéma '{}'...", schemaName);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schemaName);
            log.info("Schéma '{}' créé avec succès.", schemaName);
        } catch (Exception e) {
            log.error("Erreur lors de la création du schéma '{}'", schemaName, e);
            throw new RuntimeException("Impossible de créer le schéma.", e);
        }
    }

    /**
     * Installe (active) un module additionnel s'il existe et il est inactif.
     */
    @Transactional
    public void installAdditionalModule(String schemaName, String moduleToInstall) {
        log.info("Tentative d'installation/activation du module '{}' pour le tenant '{}'", moduleToInstall, schemaName);

        // 1. Vérifier que le module existe dans la base de données pour ce tenant.
        InstalledModule module = installedModuleRepository.findById(moduleToInstall)
                .orElseThrow(() -> new RuntimeException("Le module '" + moduleToInstall + "' est inconnu ou non pré-enregistré pour ce tenant."));

        // 2. Vérifier s'il est déjà actif.
        if (module.isActive()) {
            log.warn("Le module '{}' est déjà actif pour le tenant '{}'. Aucune action requise.", moduleToInstall, schemaName);
            // On peut lancer une exception pour un retour plus clair au client si on le souhaite.
            throw new IllegalStateException("Le module '" + moduleToInstall + "' est déjà installé et actif.");
        }

        // 3. Le module existe et est inactif, on procède à la migration.
        // On récupère tous les modules qui seront actifs APRÈS cette installation.
        List<String> modulesToMigrate = installedModuleRepository.findAll()
                .stream()
                .filter(m -> m.isActive() || m.getModuleName().equals(moduleToInstall))
                .map(InstalledModule::getModuleName)
                .collect(Collectors.toList());

        String[] migrationLocations = modulesToMigrate.stream()
                .map(name -> "classpath:db/migration/" + name)
                .toArray(String[]::new);

        log.info("Lancement de la migration pour le(s) module(s) : {}", modulesToMigrate);

        Flyway flyway = Flyway.configure()
                .dataSource(this.dataSource)
                .schemas(schemaName)
                .locations(migrationLocations)
                .load();
        flyway.migrate();

        // 4. Mettre à jour le statut du module à 'true'
        module.setActive(true);
        installedModuleRepository.save(module);
        log.info("Module '{}' activé et migré avec succès pour le tenant '{}'.", moduleToInstall, schemaName);
    }
}