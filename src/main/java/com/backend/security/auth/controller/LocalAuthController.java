package com.backend.security.auth.controller;

import com.backend.security.auth.dto.LoginRequestDTO;
import com.backend.security.auth.dto.RefreshRequestDTO;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.exception.AuthError;
import com.backend.security.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API (JWT + Refresh)")
@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class LocalAuthController {

    private final AuthService authService;

    @Operation(summary = "로그인 → Access/Refresh 동시 발급")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (AuthError e) {
            return unauthorized(e.getCode(), e.getDesc());
        }
    }

    @Operation(summary = "재발급 → Access 교체, Refresh 로테이션(보안 권장)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody RefreshRequestDTO req) {
        try {
            return ResponseEntity.ok(authService.refresh(req));
        } catch (AuthError e) {
            return unauthorized(e.getCode(), e.getDesc());
        }
    }

    @Operation(summary = "로그아웃 → Access 블랙리스트 등록 + Refresh 제거")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            authService.logout(authorizationHeader);
            return ResponseEntity.noContent().build();
        } catch (AuthError e) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.WWW_AUTHENTICATE,
                            "Bearer error=\"" + e.getCode() + "\", error_description=\"" + e.getDesc() + "\"")
                    .build();
        }
    }

    private ResponseEntity<TokenResponseDTO> unauthorized(String code, String desc) {
        return ResponseEntity.status(401)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"" + code + "\", error_description=\"" + desc + "\"")
                .body(null);
    }
}
