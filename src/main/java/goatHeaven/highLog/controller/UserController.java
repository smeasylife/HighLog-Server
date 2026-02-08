package goatHeaven.highLog.controller;

import goatHeaven.highLog.dto.request.ChangePasswordRequest;
import goatHeaven.highLog.dto.request.DeleteAccountRequest;
import goatHeaven.highLog.dto.response.AccountInfoResponse;
import goatHeaven.highLog.dto.response.DashboardResponse;
import goatHeaven.highLog.dto.response.MessageResponse;
import goatHeaven.highLog.security.CustomUserPrincipal;
import goatHeaven.highLog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(userService.getDashboard(principal.getUserId()));
    }

    @GetMapping("/accountInfo")
    public ResponseEntity<AccountInfoResponse> getAccountInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(userService.getAccountInfo(principal.getUserId()));
    }

    @PatchMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(principal.getUserId(), request));
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteAccount(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody DeleteAccountRequest request) {

        return ResponseEntity.ok(userService.deleteAccount(principal.getUserId(), request));
    }
}
