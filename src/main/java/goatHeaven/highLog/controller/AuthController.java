package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.*;
import goatHeaven.highLog.dto.response.*;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerifyResponse> sendVerificationEmail(
            @Valid @RequestBody EmailVerifyRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<EmailConfirmResponse> confirmEmail(
            @Valid @RequestBody EmailConfirmRequest request) {
        return ResponseEntity.ok(authService.confirmOtp(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(principal.getUserId(), request));
    }
}
