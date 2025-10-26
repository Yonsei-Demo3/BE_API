package com.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.member.domain.Member;
import com.backend.member.dto.SignUpRequestDTO;
import com.backend.member.dto.UpdateNicknameRequestDTO;
import com.backend.member.dto.MemberResponseDTO;
import com.backend.member.service.MemberService;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService) { this.memberService = memberService; }

    @Operation(summary = "회원 가입")
    @PostMapping
    public ResponseEntity<MemberResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO req) {
        Member saved = memberService.signUp(req);
        return ResponseEntity.ok(MemberResponseDTO.from(saved));
    }

    @Operation(summary = "회원 단일 조회")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> get(@PathVariable Long id) {
        Member found = memberService.get(id);
        return ResponseEntity.ok(MemberResponseDTO.from(found));
    }

    @Operation(summary = "회원 목록 조회 (페이지)")
    @GetMapping
    public ResponseEntity<Page<MemberResponseDTO>> list(Pageable pageable) {
        Page<MemberResponseDTO> page = memberService
                .list(pageable)
                .map(MemberResponseDTO::from);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/{id}/nickname")
    public ResponseEntity<MemberResponseDTO> changeNickname(
        @PathVariable Long id,
        @Valid @RequestBody UpdateNicknameRequestDTO req
    ) {
        Member updated = memberService.changeNickname(id, req);
        return ResponseEntity.ok(MemberResponseDTO.from(updated));
    }
    
    @Operation(summary = "회원 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
