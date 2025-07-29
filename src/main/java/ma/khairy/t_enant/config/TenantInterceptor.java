package ma.khairy.t_enant.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-TenantID");
        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // Optionnel : rejeter la requête si le tenant est manquant
            // response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // response.getWriter().write("X-TenantID header is missing");
            // return false;

            // Ou utiliser le tenant par défaut
            TenantContext.setCurrentTenant(TenantContext.DEFAULT_TENANT_ID);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // TRÈS IMPORTANT : Nettoyer le contexte pour éviter les fuites vers la prochaine requête
        TenantContext.clear();
    }
}