package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.LoginRequest;
import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserMeResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserResponse;
import org.architect.multitenantappointmentsystem.entity.Appointment;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.repository.AppointmentRepository;
import org.architect.multitenantappointmentsystem.repository.UserRepository;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.architect.multitenantappointmentsystem.security.JwtService;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email allaqachon ro'yxatdan o'tgan");
        }
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus().name()
        );
        return new AuthResponse(token, userResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            AuthUser authUser = (AuthUser) authenticate.getPrincipal();
            User user = userRepository.findById(authUser.getUserId()).orElseThrow(
                    () -> new BusinessException("User Topilmadi")
            );
            String token = jwtService.generateToken(user.getEmail(), user.getId());

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getStatus().name()
            );
            return new AuthResponse(token, userResponse);
        } catch (BadCredentialsException e) {
            throw new BusinessException("Email yoki password noto'g'ri");
        } catch (DisabledException e) {
            throw new BusinessException("Akkaunt faol emas");
        } catch (LockedException e) {
            throw new BusinessException("Akkaunt bloklangan");
        } catch (AuthenticationException e) {
            throw new BusinessException("Login xatosi");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<UserMeResponse> getMe() {
        Optional<AuthUser> optional = AuthService.getCurrentUser();
        if (optional.isEmpty()) {
            return ResponseDto.unauthorized();
        }

        AuthUser authUser = optional.get();

        return userRepository.findById(authUser.getUserId())
                .map(user -> {
                    List<String> roles = user.getStaffRoles()
                            .stream()
                            .map(staff -> staff.getRole().name())
                            .toList();

                    java.util.UUID tenantId = null;
                    String tenantSlug = null;
                    java.util.UUID staffId = null;
                    java.util.UUID staffTenantId = null;
                    String staffTenantSlug = null;

                    for (var staff : user.getStaffRoles()) {
                        if (staff.getRole() == org.architect.multitenantappointmentsystem.entity.StaffRole.OWNER) {
                            tenantId = staff.getTenant().getId();
                            tenantSlug = staff.getTenant().getSlug();
                        } else {
                            staffId = staff.getId();
                            staffTenantId = staff.getTenant().getId();
                            staffTenantSlug = staff.getTenant().getSlug();
                        }
                    }

                    return new UserMeResponse(
                            user.getId(),
                            user.getFirstName() + " " + user.getLastName(),
                            user.getPhone(),
                            user.getEmail(),
                            roles,
                            tenantId,
                            tenantSlug,
                            staffId,
                            staffTenantId,
                            staffTenantSlug
                    );
                })
                .map(ResponseDto::ok)
                .orElseGet(ResponseDto::unauthorized);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<UserResponse> findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Foydalanuvchi topilmadi: " + email));
        UserResponse response = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus().name()
        );
        return ResponseDto.ok(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<List<AppointmentResponse>> getMyAppointments(String type) {
        Optional<AuthUser> optional = AuthService.getCurrentUser();
        if (optional.isEmpty()) {
            return ResponseDto.unauthorized();
        }

        AuthUser authUser = optional.get();
        User user = userRepository.findById(authUser.getUserId()).orElse(null);
        if (user == null) {
            return ResponseDto.unauthorized();
        }

        String email = user.getEmail();
        List<Appointment> appointments;

        if ("upcoming".equalsIgnoreCase(type)) {
            appointments = appointmentRepository.findUpcomingAppointmentsByEmail(
                    email, LocalDate.now());
        } else {
            appointments = appointmentRepository.findPastAppointmentsByEmail(email);
        }

        List<AppointmentResponse> responses = appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .toList();

        return ResponseDto.ok(responses);
    }
}
