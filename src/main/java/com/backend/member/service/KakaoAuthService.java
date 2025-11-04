package com.backend.member.service;

import com.backend.member.domain.Member;
import com.backend.member.domain.SocialProvider;
import com.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final MemberRepository memberRepo;
    private final PasswordEncoder encoder;

    /**
     * 소셜 사용자 upsert: (provider, socialId)가 존재하면 업데이트, 없으면 신규 생성
     * - password: 소셜은 로그인에 쓰지 않지만 null 금지를 위해 더미 해시 저장
     */
    @Transactional
    public Member upsertKakaoUser(String socialId, String email, String nickname, String profileImageUrl) {
        return memberRepo.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)
                .map(m -> {
                    // 이메일/프로필/닉네임 최신화(필요시)
                    if (email != null && !email.equals(m.getEmail())) {
                        // 이메일 unique라 변경 시 유효성 체크 필요 (케이스에 따라 유지 권장)
                    }
                    if (nickname != null) m.changeNickname(nickname);
                    if (profileImageUrl != null) m.changeProfile(profileImageUrl);
                    return m;
                })
                .orElseGet(() -> {
                    String dummy = encoder.encode("kakao_" + socialId); // 더미 해시
                    // Member.social(...) 팩토리 사용 (이전에 만들어둔 메서드)
                    return memberRepo.save(
                            Member.social(
                                    com.backend.member.domain.SocialProvider.KAKAO,
                                    socialId,
                                    email != null ? email : ("kakao-" + socialId + "@example.com"),
                                    dummy,
                                    nickname != null ? nickname : "kakao_user"
                            )
                    );
                });
    }
}
