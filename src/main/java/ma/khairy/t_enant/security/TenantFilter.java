package ma.khairy.t_enant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.khairy.t_enant.config.TenantContext;
import ma.khairy.t_enant.exception.MissingTenantException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = null;
        String requestURI = request.getRequestURI();

        // Stratégie 1 : Extraire l'ID du tenant depuis le chemin de l'URL pour le provisioning
        if (requestURI.startsWith("/tenants/")) {
            String[] pathParts = requestURI.split("/");
            if (pathParts.length > 2) {
                tenantId = pathParts[2]; // ex: /tenants/tenant_r -> tenantId = "tenant_r"
            }
        } else {
            // Stratégie 2 : Extraire l'ID du tenant depuis l'en-tête pour toutes les autres requêtes
            tenantId = request.getHeader("X-TenantID");
        }

        try {
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setCurrentTenant(tenantId);
            } else {
                // Si aucun tenant n'a pu être déterminé et que la requête n'est pas pour le provisioning,
                // alors c'est une erreur.
                if (!requestURI.startsWith("/tenants/")) {
                    throw new MissingTenantException("L'en-tête X-TenantID est requis pour cette requête.");
                }
            }
            // On continue la chaîne de filtres avec le contexte (potentiellement) défini
            filterChain.doFilter(request, response);
        } finally {
            // Le nettoyage se fait ici, de manière centralisée, pour toutes les requêtes.
            TenantContext.clear();
        }
    }
}
