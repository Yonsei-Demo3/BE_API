package com.backend.admin.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;
import com.backend.member.repository.MemberRepository;

import java.util.List;

@Service
@Transactional
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public AdminMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 🔹 전체 회원 목록 조회
     * 관리자 전용: 모든 회원 정보 반환
     */
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * 🔹 특정 회원 조회
     * @param memberId 조회할 회원 ID
     */
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원이 존재하지 않습니다."));
    }

    /**
     * 🔹 회원을 관리자(ADMIN)로 승격
     * @param memberId 승격할 회원 ID
     */
    public Member promoteToAdmin(Long memberId) {
        Member member = findMemberById(memberId);
        member.changeRole(Role.ADMIN);
        return memberRepository.save(member);
    }

    /**
     * 🔹 회원 삭제
     * @param memberId 삭제할 회원 ID
     */
    public void deleteMember(Long memberId) {
        Member member = findMemberById(memberId);
        memberRepository.delete(member);
    }

    /**
     * 🔹 일반 회원으로 강등 (옵션)
     * @param memberId 강등할 회원 ID
     */
    public Member demoteToUser(Long memberId) {
        Member member = findMemberById(memberId);
        member.changeRole(Role.USER);
        return memberRepository.save(member);
    }
}