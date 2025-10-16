package com.backend.member.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;
import com.backend.member.dto.MemberDto;
import com.backend.member.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository repo;
    private final PasswordEncoder encoder;

    public MemberService(MemberRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Member signUp(MemberDto.SignUpRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String encodedPw = encoder.encode(req.password());
        return repo.save(Member.of(req.email(), encodedPw, req.nickname()));
    }

    public Member get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
    }

    public Page<Member> list(Pageable pageable) { return repo.findAll(pageable); }

    public Member changeNickname(Long id, MemberDto.UpdateNicknameRequest req) {
        Member m = get(id);
        m.changeNickname(req.nickname());
        return m;
    }

    public Member promoteToAdmin(Long id) {
        Member m = get(id);
        m.changeRole(Role.ADMIN);
        return m;
    }

    public void delete(Long id) { repo.deleteById(id); }
}