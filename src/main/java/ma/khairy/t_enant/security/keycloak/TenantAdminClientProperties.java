package ma.khairy.t_enant.security.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "tenants")
public class TenantAdminClientProperties {

    // Ceci va mapper toutes les propriétés sous "tenants.admin-clients"
    // La clé de la Map sera le nom du tenant (ex: "tenant_a")
    private Map<String, AdminClientConfig> adminClients;

    @Data
    public static class AdminClientConfig {
        private String clientId;
        private String clientSecret;
    }
}