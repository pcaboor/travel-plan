package com.travelplan.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplan.auth.api.dto.LoginRequest;
import com.travelplan.auth.api.dto.LoginResponse;
import com.travelplan.auth.api.dto.UserResponse;
import com.travelplan.auth.domain.AuthUser;
import com.travelplan.auth.domain.AuthUserStatus;
import com.travelplan.auth.jwt.JwtService;
import com.travelplan.auth.repository.AuthUserRepository;

@Service
public class AuthenticationService {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(AuthUserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AuthUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != AuthUserStatus.ACTIVE) {
            throw new DisabledException("Account is not active");
        }

        String token = jwtService.issueAccessToken(user);
        long expiresIn = jwtService.accessTokenTtl().toSeconds();
        return LoginResponse.of(token, expiresIn, UserResponse.from(user));
    }
}
