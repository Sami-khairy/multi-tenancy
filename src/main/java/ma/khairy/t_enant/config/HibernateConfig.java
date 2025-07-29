package ma.khairy.t_enant.config;


import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            CurrentTenantIdentifierResolver tenantIdentifierResolver,
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider) {

        return properties -> {
            properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
            // On utilise la clé en chaîne de caractères, c'est la méthode la plus sûre
            properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
        };
    }
}