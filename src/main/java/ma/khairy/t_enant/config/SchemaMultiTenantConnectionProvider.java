package ma.khairy.t_enant.config;


import ma.khairy.t_enant.exception.TenantNotFoundException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement; // On utilise un PreparedStatement
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final Logger log = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);
    private final transient DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();

        // ÉTAPE 1 : VÉRIFIER SI LE SCHÉMA EXISTE VRAIMENT
        // On utilise un PreparedStatement pour se protéger contre l'injection SQL
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM information_schema.schemata WHERE schema_name = ?")) {
            ps.setString(1, tenantIdentifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Si aucun résultat n'est retourné, le schéma n'existe pas.
                    throw new TenantNotFoundException("Tenant '" + tenantIdentifier + "' non trouvé.");
                }
            }
        } catch (SQLException e) {
            // Gérer les erreurs potentielles lors de la vérification
            throw new SQLException("Impossible de vérifier l'existence du tenant '" + tenantIdentifier + "'", e);
        }

        // ÉTAPE 2 : SI LE SCHÉMA EXISTE, ON LE DÉFINIT
        log.info("Get connection for tenant {}", tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("SET SCHEMA '%s'", tenantIdentifier));
        } catch (SQLException e) {
            // Cette erreur ne devrait plus se produire pour un schéma inexistant, mais on la garde par sécurité.
            throw new SQLException("Impossible de changer de schéma pour le tenant '" + tenantIdentifier + "'", e);
        }

        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        log.info("Release connection for tenant {}", tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            // On retourne au schéma de réinitialisation pour nettoyer la connexion
            statement.execute(String.format("SET SCHEMA '%s'", TenantContext.RESET_TENANT_ID));
        } catch (SQLException e) {
            log.error("Could not reset schema for connection of tenant {}", tenantIdentifier, e);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}