package org.jjuni.swaggerjwt.config;


import lombok.RequiredArgsConstructor;
import org.jjuni.swaggerjwt.auth.jwt.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // DB 드라이버 클래스 이름 (h2 사용 시 security 충돌 해결 위해)
    @Value("${spring.datasource.driver-class-name}")
    private String springDatasourceDriverClassName;

    // swagger, 로그인 예외 경로 설정
    private static final String[] excludePath = {
            "/",
            "/swagger-ui/**",
            "/swagger.html",
//            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-resource/**",
            "/api/v1/auth/sign-in",
            "/api/v1/auth/sign-up"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthorizationFilter jwtAuthorizationFilter)
            throws Exception {

        httpSecurity
                .csrf(csrf -> csrf.disable())
                .httpBasic(http -> http.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter.class) // jwt 필터 추가
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers(excludePath).permitAll()
                        .anyRequest().authenticated());

        if (springDatasourceDriverClassName.equals("org.h2.Driver")) {
            // h2 관련 옵션
            httpSecurity.headers(config -> config.frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin()));
        }
        return httpSecurity.getOrBuild();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(excludePath);
    }
}
