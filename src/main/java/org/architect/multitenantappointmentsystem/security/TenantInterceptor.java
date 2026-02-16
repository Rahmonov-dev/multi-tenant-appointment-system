package org.architect.multitenantappointmentsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final List<String> WHITELIST = List.of(
            "/api/auth",
            "/tenants/{slug}/all",
            "/tenants/{slug}/by-key/{tenantKey}",
            "/tenants/{slug}/by-slug",
            "/api/tenants",
            "/swagger-ui",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        try {
            String path = request.getRequestURI();

            if (WHITELIST.stream().anyMatch(path::startsWith)) {
                return true;
            }

            String tenantHeader = request.getHeader("X-Tenant-Id");
            if (tenantHeader == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return false;
            }

            Long tenantId = Long.parseLong(tenantHeader);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof AuthUser user)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

//            AuthUser user = (AuthUser) auth.getPrincipal();
            if (!user.getTenantIds().contains(tenantId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            TenantContext.setTenantId(tenantId);
            return true;

        } catch (Exception e) {
            TenantContext.clear();
            throw e;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        TenantContext.clear();
    }
}
