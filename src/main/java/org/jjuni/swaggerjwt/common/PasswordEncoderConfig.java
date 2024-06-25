package org.jjuni.swaggerjwt.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 비밀번호는 외부로 유출되면 안되기 때문에 HASH 처리 이후 단방향 암호화
        return new BCryptPasswordEncoder();
    }
}
