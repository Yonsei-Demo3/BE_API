package com.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.backend.member.dto.LocalSignUpRequestDTO;
import com.backend.member.dto.MemberResponseDTO;
import com.backend.member.dto.UpdateNicknameRequestDTO;
import com.backend.member.service.MemberService;
import com.backend.security.auth.user.CustomUserPrincipal;


@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService) { this.memberService = memberService; }

    @Operation(summary = "회원 가입")
    @PostMapping
    public ResponseEntity<MemberResponseDTO> signUp(@Valid @RequestBody LocalSignUpRequestDTO req) {
        return ResponseEntity.ok(memberService.signUp(req));
    }

    @Operation(summary = "회원 단일 조회")
    @GetMapping("/{user_id}")
    public ResponseEntity<MemberResponseDTO> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(memberService.getByUserId(userId));
    }

    @Operation(summary = "회원 목록 조회 (페이지)")
    @GetMapping
    public ResponseEntity<Page<MemberResponseDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(memberService.list(pageable));
    }

    @Operation(summary = "내 닉네임 변경")
    @PatchMapping("/me/nickname")
    public ResponseEntity<MemberResponseDTO> changeMyNickname(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @Valid @RequestBody UpdateNicknameRequestDTO req
    ) {
        return ResponseEntity.ok(memberService.changeNicknameByUserId(me.getUserId(), req));
    }
    
    @Operation(summary = "본인 회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal CustomUserPrincipal me) {
        memberService.deleteByUserId(me.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberResponseDTO> getMe(@AuthenticationPrincipal CustomUserPrincipal me) {
        return ResponseEntity.ok(memberService.getByUserId(me.getUserId()));
    }
}
