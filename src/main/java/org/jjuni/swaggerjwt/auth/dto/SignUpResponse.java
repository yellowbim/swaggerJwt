package org.jjuni.swaggerjwt.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jjuni.swaggerjwt.member.entity.Member;

@Data
@AllArgsConstructor
public class SignUpResponse {
    @Schema(description = "회원 고유 id", example = "123")
    private Long id;

    @Schema(description = "회원 id", example = "test1111")
    private String userId;

    @Schema(description = "연락처", example = "010-1234-1234")
    private String phoneNum;

    @Schema(description = "이메일", example = "test1111@naver.com")
    private String email;

    @Schema(description = "회원 이름", example = "이정준")
    private String name;

    @Schema(description = "회원 유형", example = "USER")
    private org.jjuni.swaggerjwt.auth.enums.RoleType RoleType;

    public static SignUpResponse toDto(Member member) {
        return new SignUpResponse(member.getId(),
                member.getUserId(),
                member.getPhoneNum(),
                member.getEmail(),
                member.getName(),
                member.getRole());
    }
}
