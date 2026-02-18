package org.architect.multitenantappointmentsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.architect.multitenantappointmentsystem.repository.TenantRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantRepository tenantRepository;

    private static final List<String> WHITELIST = List.of(
            "/api/auth",
            "/api/tenant",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars");

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        String path = request.getRequestURI();

        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return true;
        }

        String slug = extractSlugFromPath(request);
        if (slug == null) {
            log.warn("Slug not found in URL: {}", path);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return false;
        }

        Tenant tenant = tenantRepository.findBySlug(slug).orElse(null);
        if (tenant == null) {
            log.warn("Tenant not found for slug: {}", slug);
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Tenant not found or you are trying to acces to another tenant");
            return false;
        }

        if (!tenant.getIsActive()) {
            log.warn("Tenant is inactive: {}", slug);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Tenant is inactive");
            return false;
        }

        String tenantHeader = request.getHeader("X-Tenant-Id");
        if (tenantHeader != null) {
            try {
                Long headerTenantId = Long.parseLong(tenantHeader);
                if (!tenant.getId().equals(headerTenantId)) {
                    log.warn("Tenant ID mismatch! URL slug: {}, Header: {}", slug, headerTenantId);
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Tenant-Id header: {}", tenantHeader);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID");
                return false;
            }
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("User not authenticated");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return false;
        }

        AuthUser user = (AuthUser) auth.getPrincipal();
        if (!user.getTenantIds().contains(tenant.getId())) {
            log.warn("User {} does not have access to tenant {}", user.getEmail(), slug);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied to this tenant");
            return false;
        }

        TenantContext.setTenantId(tenant.getId());

        log.debug("Tenant context set: id={}, slug={}, user={}", tenant.getId(), slug, user.getEmail());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        TenantContext.clear();
    }


    private String extractSlugFromPath(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null && pathVariables.containsKey("slug")) {
            return pathVariables.get("slug");
        }

        String path = request.getRequestURI();
        String[] parts = path.split("/");
        if (parts.length > 1) {
            return parts[1];
        }

        return null;
    }


    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = String.format("{\"success\":false,\"message\":\"%s\"}", message);
        response.getWriter().write(json);
    }
}
