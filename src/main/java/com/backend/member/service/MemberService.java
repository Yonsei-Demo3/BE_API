package com.backend.member.service;

import com.backend.member.domain.Member;
import com.backend.member.domain.Role;
import com.backend.member.dto.LocalSignUpRequestDTO;
import com.backend.member.dto.MemberResponseDTO;
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
     * 로컬 회원가입
     */
    @Transactional
    public MemberResponseDTO signUp(LocalSignUpRequestDTO req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPw = encoder.encode(req.password());

        // UUID userId는 @PrePersist에서 자동 생성됨
        Member newMember = Member.local(
                null,               // userId (null → 자동 UUID)
                req.email(),
                encodedPw,
                req.nickname()
        );

        Member saved = repo.save(newMember);
        return MemberResponseDTO.from(saved);
    }

    /**
     * 회원 단일 조회 (readOnly 트랜잭션)
     */
    public MemberResponseDTO getByUserId(String userId) {
        Member m = repo.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return MemberResponseDTO.from(m);
    }

    /**
     * 회원 목록 조회 (페이지)
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Page<MemberResponseDTO> list(Pageable pageable) {
        return repo.findAll(pageable)
                   .map(MemberResponseDTO::from);
    }

    /**
     * 닉네임 변경 (쓰기 작업)
     */
    @Transactional
    public MemberResponseDTO changeNicknameByUserId(String userId, UpdateNicknameRequestDTO req) {
        Member m = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        m.changeNickname(req.nickname());
        return MemberResponseDTO.from(m);
    }

    /**
     * 관리자 승격 (어드민 기능)
     */
    @Transactional
    public MemberResponseDTO promoteToAdmin(String userId) {
        Member m = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        m.changeRole(Role.ADMIN);
        return MemberResponseDTO.from(m);
    }

    /**
     * 회원 삭제
     */
    @Transactional
    @SuppressWarnings("null")
    public void deleteByUserId(String userId) {
        Member m = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        repo.delete(m);
    }
}
