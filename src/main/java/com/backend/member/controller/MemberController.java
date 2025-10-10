package com.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.member.domain.Member;
import com.backend.member.dto.MemberDto;
import com.backend.member.service.MemberService;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService) { this.memberService = memberService; }

    @Operation(summary = "회원 가입")
    @PostMapping
    public ResponseEntity<MemberDto.Response> signUp(@Valid @RequestBody MemberDto.SignUpRequest req) {
        Member saved = memberService.signUp(req);
        return ResponseEntity.ok(MemberDto.Response.from(saved));
    }

    @Operation(summary = "회원 단일 조회")
    @GetMapping("/{id}")
    public ResponseEntity<MemberDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(MemberDto.Response.from(memberService.get(id)));
    }

    @Operation(summary = "회원 목록 조회 (페이지)")
    @GetMapping
    public ResponseEntity<Page<MemberDto.Response>> list(Pageable pageable) {
        return ResponseEntity.ok(memberService.list(pageable).map(MemberDto.Response::from));
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/{id}/nickname")
    public ResponseEntity<MemberDto.Response> changeNickname(@PathVariable Long id,
                                                             @Valid @RequestBody MemberDto.UpdateNicknameRequest req) {
        return ResponseEntity.ok(MemberDto.Response.from(memberService.changeNickname(id, req)));
    }
    
    @Operation(summary = "회원 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
