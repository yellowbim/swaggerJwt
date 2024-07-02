package org.jjuni.swaggerjwt.auth.service;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jjuni.swaggerjwt.auth.dto.SignInRequest;
import org.jjuni.swaggerjwt.auth.dto.SignInResponse;
import org.jjuni.swaggerjwt.auth.dto.SignUpRequest;
import org.jjuni.swaggerjwt.auth.dto.SignUpResponse;
import org.jjuni.swaggerjwt.auth.entity.RefreshTokenEntity;
import org.jjuni.swaggerjwt.auth.jwt.JwtAuthorizationFilter;
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
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    /**
     * 사용자 회원가입
     *
     * @param req
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest req) throws Exception {
        // id로 사용자 정보 조회
        Optional<Member> findMember = memberRepository.findByUserId(req.getUserId());
        if (findMember.isPresent()) {
            throw new ValidationException("이미 존재하는 사용자 입니다.");
        }

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
     * - jwt refresh token이 만료되었을때 에러처리하지 않고 update를 진행하기 위함
     * @param req
     */
//    @Transactional(dontRollbackOn = ExpiredJwtException.class)
    public SignInResponse signIn(SignInRequest req) {
        // id로 사용자 정보 조회
        Optional<Member> memberInfo = memberRepository.findByUserId(req.getUserId());
        if (!memberInfo.isPresent()) { // 사용자가 존재하지 않습니다.
            throw new UsernameNotFoundException("존재하지 않는 사용자 입니다.");
        }
        Member member = memberInfo.get();

        // 사용자 비밀번호 비교 (뒤에가 암호화 되지 않은 값이 와야함!!)
        if (encoder.matches(member.getPassword(),req.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // Access Token 발급
        String accessToken = jwtUtil.createAccessToken(memberInfo.get());
        // Refresh Token 정보 초기화
        String refreshToken = null;

        // refresh token 존재 여부 확인
        // 토큰이 만료된 경우 재발급
        refreshToken  = refreshTokenRepository.findById(memberInfo.get().getId())
                .map(it -> { // refresh token 이 있는 경우
                    return validateAndRefreshToken(it, accessToken);
                })
                // refresh token 이 없는 경우
                .orElseGet(() -> {
                    log.info("신규 사용자 토큰 발급");
                    String newRefreshToken = jwtUtil.createRefreshToken();
                    refreshTokenRepository.save(new RefreshTokenEntity(member, newRefreshToken));
                    return newRefreshToken;
                });

        // access token 발급
        return new SignInResponse(memberInfo.get().getName(), memberInfo.get().getRole(), accessToken, refreshToken);
    }

    /**
     * Access Token 재발급
     *
     * @param req
     */
    @Transactional
    public String reIssueAccessToken(HttpServletRequest request, String refreshToken) throws Exception {
        // id로 사용자 정보 조회
        if (refreshToken == null) {
            throw new Exception();
        }

        // access token 발급
        return jwtAuthorizationFilter.reIssueAccessToken(request, refreshToken);
    }

    /**
     * 로그인 시 refresh token이 만료되었는지 판단하여 return 해주는 함수
     * - 만료 전 : 기존 DB 정보
     * - 만료 후 : 신규 발급 및 DB 저장
     * @param tokenEntity
     * @param accessToken
     * @return
     */
    public String validateAndRefreshToken(RefreshTokenEntity tokenEntity, String accessToken) {
        try {
            jwtUtil.validateRefreshToken(tokenEntity.getRefreshToken(), accessToken);
            log.info("Refresh Token 만료 전");
            return tokenEntity.getRefreshToken();
        } catch (ExpiredJwtException e) {
            log.info("Refresh Token 만료로 인한 재발급");
            String newRefreshToken = jwtUtil.createRefreshToken();
            tokenEntity.updateRefreshToken(newRefreshToken);
            refreshTokenRepository.save(tokenEntity);
            return newRefreshToken;
        }
    }

}