package com.backend.security.auth.controller;

import com.backend.member.domain.Member;
import com.backend.member.service.KakaoAuthService;
import com.backend.security.auth.dto.TokenResponseDTO;
import com.backend.security.auth.service.AuthTokenService;
import com.backend.security.oauth.kakao.KakaoOAuthClient;
import com.backend.security.oauth.kakao.dto.KakaoAccessTokenLoginRequestDTO;
import com.backend.security.oauth.kakao.dto.KakaoLoginRequestDTO;
import com.backend.security.oauth.kakao.dto.KakaoTokenResponseDTO;
import com.backend.security.oauth.kakao.dto.KakaoUserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SocialAuth", description = "카카오 소셜 로그인")
@RestController
@RequestMapping("/api/auth/oauth/kakao")
public class KakaoAuthController {

    private final KakaoOAuthClient kakao;
    private final KakaoAuthService socialAuthService;
    private final AuthTokenService authTokenService;

    public KakaoAuthController(KakaoOAuthClient kakao,
                                KakaoAuthService socialAuthService,
                                AuthTokenService authTokenService) {
        this.kakao = kakao;
        this.socialAuthService = socialAuthService;
        this.authTokenService = authTokenService;
    }

    @Operation(summary = "[서버 사이드] 인가코드로 카카오 로그인")
    @PostMapping("/login-by-code")
    public ResponseEntity<TokenResponseDTO> loginByCode(@Valid @RequestBody KakaoLoginRequestDTO req) {
        KakaoTokenResponseDTO token = kakao.exchangeCodeForToken(req.code());
        KakaoUserResponseDTO user = kakao.fetchUser(token.accessToken());

        String socialId = String.valueOf(user.id());
        String email = user.kakaoAccount() != null ? user.kakaoAccount().email() : null;
        String nickname = (user.kakaoAccount() != null && user.kakaoAccount().profile() != null)
                ? user.kakaoAccount().profile().nickname() : null;
        String profile = (user.kakaoAccount() != null && user.kakaoAccount().profile() != null)
                ? user.kakaoAccount().profile().profileImageUrl() : null;

        Member m = socialAuthService.upsertKakaoUser(socialId, email, nickname, profile);
        return ResponseEntity.ok(authTokenService.issueTokensFor(m));
    }

    @Operation(summary = "[프론트/모바일] 카카오 access_token으로 로그인")
    @PostMapping("/login-by-token")
    public ResponseEntity<TokenResponseDTO> loginByToken(@Valid @RequestBody KakaoAccessTokenLoginRequestDTO req) {
        KakaoUserResponseDTO user = kakao.fetchUser(req.kakaoAccessToken());

        String socialId = String.valueOf(user.id());
        String email = user.kakaoAccount() != null ? user.kakaoAccount().email() : null;
        String nickname = (user.kakaoAccount() != null && user.kakaoAccount().profile() != null)
                ? user.kakaoAccount().profile().nickname() : null;
        String profile = (user.kakaoAccount() != null && user.kakaoAccount().profile() != null)
                ? user.kakaoAccount().profile().profileImageUrl() : null;

        Member m = socialAuthService.upsertKakaoUser(socialId, email, nickname, profile);
        return ResponseEntity.ok(authTokenService.issueTokensFor(m));
    }
    
}
