package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.LoginRequest;
import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserMeResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserResponse;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    ResponseDto<UserMeResponse> getMe();
    ResponseDto<List<AppointmentResponse>> getMyAppointments(String type);
    ResponseDto<UserResponse> findUserByEmail(String email);

    static Optional<AuthUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AuthUser user) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    static java.util.UUID getCurrentUserId() {
        try {
            return getCurrentUser()
                    .map(AuthUser::getUserId)
                    .orElseThrow(() -> new AccessDeniedException("User topilmadi"));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }
}
