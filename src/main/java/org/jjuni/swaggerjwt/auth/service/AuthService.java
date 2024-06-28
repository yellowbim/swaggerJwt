package org.jjuni.swaggerjwt.auth.service;


import jakarta.transaction.Transactional;
import jakarta.xml.bind.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jjuni.swaggerjwt.auth.dto.*;
import org.jjuni.swaggerjwt.auth.jwt.JwtUtil;
import org.jjuni.swaggerjwt.auth.repository.RefreshTokenRepository;
import org.jjuni.swaggerjwt.member.entity.Member;
import org.jjuni.swaggerjwt.member.repository.MemberRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    /**
     * 사용자 회원가입
     *
     * @param req
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest req) throws Exception {
        // id로 사용자 정보 조회
        memberRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ValidationException("이미 회원가입한 고객입니다."));

        Member newMember = req.toEntity();
        try {
            newMember.setPassword(encoder.encode(req.getPassword()));
        } catch (Exception e) {
            throw new Exception(e);
        }

        // 사용자 등록
        try {
            memberRepository.save(newMember);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return SignUpResponse.toDto(newMember);
    }

    /**
     * 사용자 로그인
     *
     * @param req
     */
    @Transactional
    public SignInResponse signIn(SignInRequest req) {
        // id로 사용자 정보 조회
        Optional<Member> memberInfo = memberRepository.findByUserId(req.getUserId());
        if (memberInfo.get() == null) { // 사용자가 존재하지 않습니다.
            throw new UsernameNotFoundException("존재하지 않는 사용자 입니다.");
        }

        // 사용자 비밀번호 비교 (뒤에가 암호화 되지 않은 값이 와야함!!)
        if (encoder.matches(memberInfo.get().getPassword(),req.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // Access 발급
        String accessToken = jwtUtil.createAccessToken(memberInfo.get());
        String refreshToken = jwtUtil.createRefreshToken();

        // refresh token 존재 여부 확인
//        refreshTokenRepository.findById(memberInfo.get().getId())
//                .ifPresentOrElse(
//                        it -> it.updateRefreshToken(refreshToken),
//                        () -> refreshTokenRepository.
//                );

//        refreshTokenRepository.save(refreshToken);



        // access token 발급
        return new SignInResponse(memberInfo.get().getName(), memberInfo.get().getRole(), jwtUtil.createAccessToken(memberInfo.get()), "");
    }
}