package org.architect.multitenantappointmentsystem.controller;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.LoginRequest;
import org.architect.multitenantappointmentsystem.dto.request.RegisterRequest;
import org.architect.multitenantappointmentsystem.dto.response.AuthResponse;
import org.architect.multitenantappointmentsystem.dto.response.UserMeResponse;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<AuthResponse>> register(@Validated @RequestBody RegisterRequest request) {
        return ResponseDto.ok(service.register(request)).toResponseEntity();
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<AuthResponse>> login(@Validated @RequestBody LoginRequest request) {
        return ResponseDto.ok(service.login(request)).toResponseEntity();
    }
    @GetMapping("/me")
    public ResponseEntity<ResponseDto<UserMeResponse>> me() {
        return service.getMe().toResponseEntity();
    }
}
