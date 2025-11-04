package com.backend.admin.controller;

import com.backend.admin.service.AdminMemberService;
import com.backend.member.domain.Member;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(adminMemberService.findAllMembers());
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<Member> promoteToAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(adminMemberService.promoteToAdmin(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        adminMemberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}