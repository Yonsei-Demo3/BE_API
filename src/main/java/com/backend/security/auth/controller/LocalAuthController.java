package com.backend.security.auth.controller;

import com.backend.security.auth.dto.LoginRequestDTO;
import com.backend.security.auth.dto.TokenIssueResultDTO;
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
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "Auth", description = "인증 API (JWT + Refresh)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LocalAuthController {

    private final AuthService authService;
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    @Operation(summary = "로그인 → Access는 바디, Refresh는 HttpOnly 쿠키로 발급")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDTO req) {
        try {
            TokenIssueResultDTO issued = authService.login(req);

            // Refresh는 HttpOnly 쿠키로 내려보내기
            ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, issued.refreshToken())
                    .httpOnly(true)
                    .secure(false)          // TODO: 추후 true로 변경
                    .path("/")
                    .maxAge(issued.refreshTokenExpiresIn())
                    .sameSite("Lax")
                    .build();

            // Authorization 헤더에 넣을 값
            String bearerToken = "Bearer " + issued.accessToken();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .build();

        } catch (AuthError e) {
            // 여기서 T = AccessTokenResponseDTO 로 추론됨
            return unauthorized(e.getCode(), e.getDesc());
        }
    }

    @Operation(summary = "로그아웃 → Access 블랙리스트 등록 + Refresh 쿠키 제거")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.logout(authorizationHeader);

            // refreshToken 쿠키 즉시 만료 시키기
            ResponseCookie deleteRefreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                    .httpOnly(true)
                    .secure(false)      // TODO: 추후 true로 변경
                    .path("/")
                    .maxAge(0)          // 즉시 만료
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                    .build();

        } catch (AuthError e) {
            return ResponseEntity.status(401)
                    .header(HttpHeaders.WWW_AUTHENTICATE,
                            "Bearer error=\"" + e.getCode() + "\", error_description=\"" + e.getDesc() + "\"")
                    .build();
        }
    }

    @Operation(summary = "재발급 → 쿠키의 Refresh로 Access 재발급 + Refresh 로테이션")
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie
    ) {
        try {
            // 1) 쿠키 없으면 바로 401
            if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                throw AuthError.invalidRefreshToken();  // 네가 정의한 팩토리 메서드/코드에 맞게
            }

            // 2) 서비스 호출 → 새 Access/Refresh 발급 + DB RT 회전
            TokenIssueResultDTO issued = authService.refresh(refreshTokenCookie);

            // 3) 새 Refresh를 다시 HttpOnly 쿠키로 설정 (로테이션)
            ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, issued.refreshToken())
                    .httpOnly(true)
                    .secure(true)          // HTTPS 환경이면 true 유지
                    .path("/")
                    .maxAge(issued.refreshTokenExpiresIn())
                    .sameSite("Lax")
                    .build();

            String bearerToken = issued.tokenType() + " " + issued.accessToken();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .build();

        } catch (AuthError e) {
            // refresh가 잘못됐으면 쿠키도 같이 죽이기(보안상 안전)
            ResponseCookie clearCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .header(HttpHeaders.WWW_AUTHENTICATE,
                            "Bearer error=\"" + e.getCode() + "\", error_description=\"" + e.getDesc() + "\"")
                    .build();
        }
    }

    /**
     * 401 공통 응답 생성용 (바디는 null)
     * 호출하는 쪽의 제네릭 타입에 맞게 알아서 맞춰줌
     */
    private <T> ResponseEntity<T> unauthorized(String code, String desc) {
        return ResponseEntity.status(401)
                .header(HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer error=\"" + code + "\", error_description=\"" + desc + "\"")
                .body(null);
    }
}
