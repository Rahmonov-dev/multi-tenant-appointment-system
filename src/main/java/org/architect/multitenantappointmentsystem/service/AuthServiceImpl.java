package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.LoginRequest;
import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserMeResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserResponse;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
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
        Authentication authenticate =authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password())
        );

            AuthUser authUser= (AuthUser) authenticate.getPrincipal();
            User user= userRepository.findById(authUser.getUserId()).orElseThrow(
                    ()-> new BusinessException("User Topilmadi")
            );
            String token = jwtService.generateToken(user.getEmail(),user.getId());

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getStatus().name()
            );
            return new AuthResponse(token, userResponse);
        }catch (BadCredentialsException e) {
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
    public ResponseDto<UserMeResponse> getMe() {

        Optional<AuthUser> optional = AuthService.getCurrentUser();
        if (optional.isEmpty()) {
            return ResponseDto.unauthorized();
        }

        AuthUser authUser = optional.get();

        return userRepository.findById(authUser.getUserId())
                .map(user -> new UserMeResponse(
                        user.getId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getPhone(),
                        user.getEmail(),
                        user.getStaffRoles()
                                .stream()
                                .map(staff -> staff.getRole().name())
                                .toList()
                ))
                .map(ResponseDto::ok)
                .orElseGet(ResponseDto::unauthorized);
    }

}
