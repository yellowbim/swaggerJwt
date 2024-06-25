package org.jjuni.swaggerjwt.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jjuni.swaggerjwt.auth.enums.RoleType;

/**
 * 로그인 응답 Dto
 */
@Data
@AllArgsConstructor
public class SignInResponse {
    @Schema(description = "회원 이름", example = "이정준")
    private String name;

    @Schema(description = "회원 유형", example = "USER")
    private RoleType RoleType;

    private String accessToken;

    private String refreshToken;
}
