package org.jjuni.swaggerjwt.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.jjuni.swaggerjwt.auth.dto.SignInRequest;
import org.jjuni.swaggerjwt.auth.dto.SignInResponse;
import org.jjuni.swaggerjwt.auth.dto.SignUpRequest;
import org.jjuni.swaggerjwt.auth.dto.SignUpResponse;
import org.jjuni.swaggerjwt.auth.service.AuthService;
import org.jjuni.swaggerjwt.common.dto.CommResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@Tag(name = "Auth", description = "회원가입, 로그인, 로그아웃 인증처리 API")
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 회원가입
     *
     * @param request
     * @return SignUpResponse
     */
    @Operation(summary = "회원가입", description = "신규 사용자 회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Create Success")
    })
    @ResponseBody
    @PostMapping("sign-up")
    public CommResponse<SignUpResponse> signUp(@Validated @RequestBody SignUpRequest request) throws Exception {
        SignUpResponse signUpResponse = authService.signUp(request);
        return CommResponse.createSuccess(signUpResponse);
    }

    /**
     * 사용자 로그인
     *
     * @param request
     * @return
     */
    @Operation(summary = "로그인", description = "사용자 로그인 (jwt 토큰 발급)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Login Success")
    })
    @ResponseBody
    @PostMapping("sign-in")
    public CommResponse<SignInResponse> signIn(@Validated @RequestBody SignInRequest request) {
        SignInResponse signInResponse = authService.signIn(request);
        return CommResponse.createSuccess(signInResponse);
    }

    /**
     * Access Token 재발급 요청
     * - Header 에 있는 Refresh Token 으로 Access Token 재발급
     *
     * @header refresh-token
     * @return
     */
    @Operation(summary = "Access Token 재발급", description = "Access Token 만료로 인한 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access Token")
    })
    @ResponseBody
    @PostMapping("reissue-access-token")
    public CommResponse<String> reIssueAccessToken(HttpServletRequest request, @RequestHeader("Refresh-Token") String refreshToken) throws Exception {
        String newAccesToken = authService.reIssueAccessToken(request, refreshToken);
        return CommResponse.createSuccess(newAccesToken);
    }
}
