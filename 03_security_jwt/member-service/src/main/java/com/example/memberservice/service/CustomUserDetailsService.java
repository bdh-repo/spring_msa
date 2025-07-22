package com.example.memberservice.service;

import com.example.memberservice.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = findMemberByUsername(username);
        
        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole()));

        return new User(
            member.getMemberId(),
            member.getPassword(),
            authorities
        );
    }

    private Member findMemberByUsername(String username) {
        // 실제 프로덕션에서는 데이터베이스에서 회원 정보를 조회
        // 여기서는 데모용으로 하드코딩된 사용자 정보 사용
        if ("testuser".equals(username)) {
            // 비밀번호를 BCrypt로 인코딩 (실제로는 DB에 저장될 때 인코딩됨)
            String encodedPassword = passwordEncoder.encode("password123");
            return new Member("testuser", encodedPassword, "USER", "테스트 사용자", "test@example.com");
        }
        
        return null; // 사용자 찾기 실패
    }

    public Member getMemberById(String memberId) {
        return findMemberByUsername(memberId);
    }
}