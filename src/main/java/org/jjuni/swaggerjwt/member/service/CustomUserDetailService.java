package org.jjuni.swaggerjwt.member.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.jjuni.swaggerjwt.member.entity.Member;
import org.jjuni.swaggerjwt.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService  {
    private final MemberRepository memberRepository;

    @Override
    public User loadUserByUsername(String userId) throws UsernameNotFoundException {
        // userId로 사용자 정보를 찾기
        Member user = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // User 객체 생성
        return new User(user.getUserId(), "", List.of(new SimpleGrantedAuthority(user.getRole().toString())));
    }

}
