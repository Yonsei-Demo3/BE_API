package com.backend.security.auth.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member m = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var auth = new SimpleGrantedAuthority("ROLE_" + m.getRole().name());
        return new User(m.getEmail(), m.getPassword(), List.of(auth));
    }
}