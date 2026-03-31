package com.mentalhealthforum.userservice.infrastructure.web;

import com.mentalhealthforum.userservice.application.dto.AuthResponse;
import com.mentalhealthforum.userservice.application.dto.LoginRequest;
import com.mentalhealthforum.userservice.application.dto.RegisterRequest;
import com.mentalhealthforum.userservice.application.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserApplicationService userApplicationService;

    public AuthController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userApplicationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userApplicationService.login(request);
        return ResponseEntity.ok(response);
    }
}
