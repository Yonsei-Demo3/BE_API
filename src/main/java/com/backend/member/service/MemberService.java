package com.backend.member.service;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;
import com.backend.member.dto.SignUpRequestDTO;
import com.backend.member.dto.UpdateNicknameRequestDTO;
import com.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository repo;
    private final PasswordEncoder encoder;

    /**
     * 회원가입 (쓰기 작업이므로 readOnly=false)
     */
    @Transactional
    public Member signUp(SignUpRequestDTO req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPw = encoder.encode(req.password());
        Member member = Member.of(req.email(), encodedPw, req.nickname());

        return repo.save(member);
    }

    /**
     * 회원 단일 조회 (readOnly 트랜잭션)
     */
    public Member get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
    }

    /**
     * 회원 목록 조회 (페이지)
     */
    public Page<Member> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * 닉네임 변경 (쓰기 작업)
     */
    @Transactional
    public Member changeNickname(Long id, UpdateNicknameRequestDTO req) {
        Member m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        m.changeNickname(req.nickname());
        return m; // JPA Dirty Checking으로 자동 업데이트
    }

    /**
     * 관리자 승격 (어드민 기능)
     */
    @Transactional
    public Member promoteToAdmin(Long id) {
        Member m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        m.changeRole(Role.ADMIN);
        return m;
    }

    /**
     * 회원 삭제
     */
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
