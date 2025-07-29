package ma.khairy.t_enant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.khairy.t_enant.config.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader("X-TenantID");
        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setCurrentTenant(tenantId);
            } else {
                // Si aucun tenant n'est fourni, on utilise le tenant par défaut.
                // Important pour les endpoints publics ou les cas sans tenant.
                TenantContext.setCurrentTenant(TenantContext.DEFAULT_TENANT_ID);
            }
            filterChain.doFilter(request, response);
        } finally {
            // TRÈS IMPORTANT : Nettoyer le contexte après la requête.
            TenantContext.clear();
        }
    }
}