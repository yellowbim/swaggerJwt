package org.jjuni.swaggerjwt.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 로그인 요청 Dto
 */
@Data
public class SignInRequest {

    @Size(min = 2, max = 50)
    @Schema(description = "사용자ID", example = "leejj9999")
    @NotBlank(message = "아이디를 입력해주세요")
    private String userId;

    @Size(min = 9, max = 30)
    @Schema(description = "비밀번호", example = "test1234!@#$")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
