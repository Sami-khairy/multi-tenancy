package ma.khairy.t_enant.flyway;

import ma.khairy.t_enant.exception.TenantNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class TenantValidationService {

    private final DataSource dataSource;

    public TenantValidationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Vérifie si un schéma existe et renvoie true/false.
     * Idéal pour une vérification avant création.
     */
    public boolean tenantExists(String schemaName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM information_schema.schemata WHERE schema_name = ?")) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si le schéma existe
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de vérifier l'existence du tenant.", e);
        }
    }

    /**
     * Vérifie si un schéma existe et lève une exception s'il n'est pas trouvé.
     * Idéal pour valider les requêtes sur des tenants existants.
     */
    public void validateTenantExists(String schemaName) {
        if (!tenantExists(schemaName)) {
            throw new TenantNotFoundException("Le tenant '" + schemaName + "' n'existe pas.");
        }
    }
}
