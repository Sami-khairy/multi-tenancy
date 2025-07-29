package ma.khairy.t_enant.security;

import ma.khairy.t_enant.config.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantJwtDecoder implements JwtDecoder {

    // Cache to store decoders for each tenant. This is crucial for performance.
    private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

    // Inject the base URL from application.properties
    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;

    @Override
    public Jwt decode(String token) throws JwtException {
        // 1. Get the current tenant ID from the ThreadLocal context
        String tenantId = TenantContext.getCurrentTenant();

        // 2. Check if a decoder for this tenant is already cached
        if (!decoders.containsKey(tenantId)) {
            // 3. If not, create a new one and cache it
            String issuerUri = keycloakBaseUrl + "/realms/" + tenantId;
            JwtDecoder newDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);
            decoders.put(tenantId, newDecoder);
        }

        // 4. Use the appropriate decoder to decode the JWT
        return decoders.get(tenantId).decode(token);
    }
}