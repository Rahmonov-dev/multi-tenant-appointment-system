package org.architect.multitenantappointmentsystem.service;

import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.repository.UserRepository;
import org.architect.multitenantappointmentsystem.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register() should save user and return token when email is new")
    void register_ShouldSaveUser_WhenEmailIsNew() {
        RegisterRequest request= new RegisterRequest("John ","Doe","john@gmail.com","password123","+998901223456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(request.email());
        savedUser.setFirstName(request.firstName());
        savedUser.setLastName(request.lastName());
        savedUser.setPhone(request.phone());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser.getEmail(), savedUser.getId())).thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mockToken");
        assertThat(response.user().email()).isEqualTo(request.email());

        verify(userRepository).save(any(User.class));
    }
    @Test
    @DisplayName("register() should throw exception when email already exists")
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "exist@example.com", "password123", "+998901234567"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email allaqachon ro'yxatdan o'tgan");

        verify(userRepository, never()).save(any(User.class));
    }


}
