package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.LoginRequest;
import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserMeResponse;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

public interface AuthService {
    public AuthResponse register(RegisterRequest request);

    public AuthResponse login(LoginRequest request);
    public ResponseDto<UserMeResponse> getMe();

    static Optional<AuthUser> getCurrentUser() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null|| !authentication.isAuthenticated()||authentication.getPrincipal()==null){
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AuthUser user) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }
    static Long getCurrentUserId() {
        try {
            return getCurrentUser()
                    .map(AuthUser::getUserId)
                    .orElseThrow(() -> new AccessDeniedException("User topilmadi"));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }
}
