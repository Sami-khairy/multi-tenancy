package ma.khairy.t_enant.flyway;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class TenantMigrationService {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(TenantMigrationService.class);

    public TenantMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void provisionTenant(String schemaName) {
        log.info("Début du provisioning pour le tenant '{}'...", schemaName);

        // 1. Créer le schéma
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            log.info("Création du schéma '{}'...", schemaName);
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (Exception e) {
            log.error("Erreur lors de la création du schéma '{}'", schemaName, e);
            throw new RuntimeException("Impossible de créer le schéma.", e);
        }

        // 2. Configurer et lancer Flyway pour ce schéma
        log.info("Lancement des migrations Flyway pour le schéma '{}'...", schemaName);
        Flyway flyway = Flyway.configure()
                .dataSource(this.dataSource)
                .schemas(schemaName) // <-- C'est ici la magie ! On cible le schéma.
                .load();

        flyway.migrate();
        log.info("Provisioning du tenant '{}' terminé avec succès.", schemaName);
    }
}