package com.travelplan.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplan.auth.api.dto.LoginRequest;
import com.travelplan.auth.api.dto.LoginResponse;
import com.travelplan.auth.api.dto.UserResponse;
import com.travelplan.auth.domain.AuthUser;
import com.travelplan.auth.jwt.JwtService.ParsedJwt;
import com.travelplan.auth.repository.AuthUserRepository;
import com.travelplan.auth.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AuthUserRepository userRepository;

    public AuthController(AuthenticationService authenticationService,
                          AuthUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof ParsedJwt parsed)) {
            return ResponseEntity.status(401).build();
        }
        AuthUser user = userRepository.findById(parsed.userId())
                .orElseThrow(() -> new IllegalStateException("User missing for valid JWT"));
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
