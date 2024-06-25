package org.jjuni.swaggerjwt.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
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
            "/console/**" // H2
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



        // 2. OPTIONS 요청일 경우 => 로직 처리 없이 다음 필터로 이동
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // [STEP1] Client에서 API를 요청할때 Header를 확인합니다.
        String authorizationHeader = request.getHeader("Authorization");

        logger.debug("[+] header Check: " + authorizationHeader);

        try {
            // [STEP2-1] Header 내에 토큰이 존재하는 경우
            if (authorizationHeader != null && !authorizationHeader.equalsIgnoreCase("")) {
                // [STEP2] Header 내에 토큰을 추출
                String accessToken = authorizationHeader.substring(7);
                // [STEP3] 추출한 토큰이 유효한지 여부를 체크
                if (jwtUtil.validateToken(accessToken)) {
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
                    // 토큰이 유효하지 않은 경우
                } else {
                    throw new BadRequestException("토큰 정보가 유효하지 않습니다.");
                }
            }
            // [STEP2-1] 토큰이 존재하지 않는 경우
            else {
                throw new JwtException("토큰 정보가 누락되어있습니다.");
            }
        } catch (Exception e) {
            // Token 내에 Exception이 발생 하였을 경우 => 클라이언트에 응답값을 반환하고 종료합니다.
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();
            JSONObject jsonObject = jsonResponseWrapper(e);
            printWriter.print(jsonObject);
            printWriter.flush();
            printWriter.close();
        }
    }

    /**
     * 토큰 관련 Exception 발생 시 예외 응답값 구성
     *
     * @param e Exception
     * @return JSONObject
     */
    private JSONObject jsonResponseWrapper(Exception e) {

        String resultMsg = "";
        // 일치하는 사용자 정보가 없는경우
        if (e instanceof UsernameNotFoundException) {
            resultMsg = "Not Found User";
        }
        // JWT 토큰 만료
        else if (e instanceof ExpiredJwtException) {
            resultMsg = "TOKEN Expired";
        }
        // JWT 허용된 토큰이 아님
        else if (e != null) {
            resultMsg = "TOKEN SignatureException Login";
        }
        // JWT 토큰내에서 오류 발생 시
        else if (e instanceof JwtException) {
            resultMsg = "TOKEN Parsing JwtException";
        }
        // 이외 JTW 토큰내에서 오류 발생
        else {
            resultMsg = "OTHER TOKEN ERROR";
        }
        HashMap<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("status", 401);
        jsonMap.put("code", "9999");
        jsonMap.put("message", resultMsg);
        jsonMap.put("reason", e.getMessage());
        JSONObject jsonObject = new JSONObject(jsonMap);
        logger.error(resultMsg, e);
        return jsonObject;
    }
}
