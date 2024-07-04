package org.jjuni.swaggerjwt.auth.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jjuni.swaggerjwt.common.dto.CommResponse;
import org.jjuni.swaggerjwt.common.enums.ResultCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * [지정한 URL 별 JWT 유효성 검증을 수행하며 직접적인 사용자 '인증'을 확인]
 *
 * @author lee
 * @fileName JwtAuthorizationFilter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/swagger.html",
            "/api-docs/**",
            "/api/v1/auth/sign-in",
            "/api/v1/auth/sign-up",
            "/api/v1/auth/reissue-access-token", // refresh token 재발급
            "/console/**", // H2
            "/favicon.ico" // icon 인데 추가 안하니까 jwt에서 계속 에러 발생
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * JWT 토큰 검증 필터 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws IOException, ServletException {
        // 요청 url 추출
        String requestURI = request.getRequestURI();

        boolean isExcludedPath = EXCLUDE_PATHS.stream()
                .anyMatch(excludePath -> pathMatcher.match(excludePath, requestURI));

        if (isExcludedPath) {
            chain.doFilter(request, response);
            return;
        }

        // OPTIONS 요청일 경우 => 로직 처리 없이 다음 필터로 이동
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // Header를 확인합니다.
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            // Header 내에 토큰이 존재하는 경우
            if (authorizationHeader != null && !authorizationHeader.equalsIgnoreCase("")) {
                // Header 내에 토큰을 추출
                String accessToken = authorizationHeader.substring(7);
                // 추출한 토큰이 유효한지 여부를 체크
                if (jwtUtil.validateAccessToken(accessToken)) {
                    // [STEP4] 토큰을 기반으로 사용자 아이디를 반환 받는 메서드
                    String userId = jwtUtil.getUserId(accessToken);
                    logger.debug("[+] user id Check: " + userId);
                    // [STEP5] 사용자 아이디가 존재하는지 여부 체크
                    if (userId != null) {
                        //[STEP6] 사용자 정보 조회 후 security context 등록 (userId로 체크)
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        chain.doFilter(request, response);
                    } else {
                        throw new UsernameNotFoundException("해당하는 사용자가 없습니다.");
                    }
                }
            }
            // 토큰이 존재하지 않는 경우
            else {
                throw new NotFoundException("토큰 정보가 누락되어있습니다.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            // Token 내에 Exception이 발생 하였을 경우 => 클라이언트에 응답값을 반환하고 종료합니다.
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            PrintWriter printWriter = response.getWriter();
            String newResponse = jsonResponseWrapper(e);
            printWriter.print(newResponse);
            printWriter.flush();
            printWriter.close();
        }
    }

    /**
     * Access Token 만료로 재발급 요청 시 Access Token 재발급
     *
     * @param request
     */
    public String reIssueAccessToken(HttpServletRequest request, String refreshToken) {
        // 만료된 Access Token 추출
        String expiredAccessToken = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
        // Refresh Token 검증(사용자 정보, DB 존재 여부, 만료 여부)
        jwtUtil.validateRefreshToken(refreshToken, expiredAccessToken);
        // Access Token 재발급
        String newAccessToken = jwtUtil.reIssueAccessToken(expiredAccessToken);
        // 사용자 정보 조회 후 security context 등록 (userId로 체크)
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtil.getUserId(newAccessToken));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return newAccessToken;
    }

    /**
     * Access Token 만료로 요청 시 Access Token 재발급
     * - 미사용
     * @param request
     * @param response
     * @param exception
     */
    private void reIssueAccessToken(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            // 만료된 Access Token 확인
            String expiredAccessToken = request.getHeader(HttpHeaders.AUTHORIZATION).substring(7);
            String refreshToken = request.getHeader("Refresh-Token");
            // Refresh Token 검증(사용자 정보, DB 존재 여부, 만료 여부)
            jwtUtil.validateRefreshToken(refreshToken, expiredAccessToken);
            // Access Toen 재발급
            String newAccessToken = jwtUtil.reIssueAccessToken(expiredAccessToken);
            // 사용자 정보 조회 후 security context 등록 (userId로 체크)
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtil.getUserId(newAccessToken));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            response.setHeader("New-Access-Token", newAccessToken);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
    }

    /**
     * 토큰 관련 Exception 발생 시 예외 응답값 구성
     *
     * @param e Exception
     * @return JSONObject
     */
    public static String jsonResponseWrapper(Exception e) throws JsonProcessingException {
        CommResponse<?> newResponse;
        // 토큰 정보가 누락된경우
        if (e instanceof NotFoundException) {
            newResponse = CommResponse.createError(ResultCode.JWT_NOT_FIND_TOKEN.getResultMessage());
        }
        // 일치하는 사용자 정보가 없는경우
        else if (e instanceof UsernameNotFoundException) {
            newResponse = CommResponse.createError(ResultCode.NOT_FOUND_USER.getResultMessage());
        }
        // JWT 토큰 만료
        else if (e instanceof ExpiredJwtException) {
            newResponse = CommResponse.createError(ResultCode.JWT_ACCESS_TOKEN_EXPIRED.getResultMessage());
        }
        // JWT 토큰내에서 오류 발생 시
        else if (e instanceof JwtException) {
            newResponse = CommResponse.createError(ResultCode.JWT_TOKEN_PARSING.getResultMessage());
        }
        // JWT 허용된 토큰이 아님
        else if (e != null) {
            newResponse = CommResponse.createError(ResultCode.UNAUTHORIZED.getResultMessage());
        }
        // 이외 JTW 토큰내에서 오류 발생
        else {
            newResponse = CommResponse.createError(e.getMessage());
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(newResponse);

        log.error(jsonResponse);
        return jsonResponse;
    }
}
