package ma.khairy.t_enant.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MultiTenantJwtDecoder multiTenantJwtDecoder;
    private final TenantFilter tenantFilter;

    public SecurityConfig(MultiTenantJwtDecoder multiTenantJwtDecoder, TenantFilter tenantFilter) {
        this.multiTenantJwtDecoder = multiTenantJwtDecoder;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Authorize all incoming HTTP requests
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/tenants/**").permitAll() // Allow tenant creation endpoint
                        .anyRequest().authenticated()
                )
                // Configure the resource server to use JWTs
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Use our custom multi-tenant decoder
                                .decoder(multiTenantJwtDecoder)
                        )
                )
                .addFilterBefore(tenantFilter, BearerTokenAuthenticationFilter.class);;

        return http.build();
    }
}