package org.jjuni.swaggerjwt.auth.dto;

import lombok.Data;

@Data
public class RefreshToken {
    private String id;
    private String member;
    private String refreshToken;
    private String refreshTokenExpireDate;
}
