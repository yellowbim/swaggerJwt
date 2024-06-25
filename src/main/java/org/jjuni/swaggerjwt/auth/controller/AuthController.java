package org.jjuni.swaggerjwt.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jjuni.swaggerjwt.auth.dto.SignInRequest;
import org.jjuni.swaggerjwt.auth.dto.SignInResponse;
import org.jjuni.swaggerjwt.auth.dto.SignUpRequest;
import org.jjuni.swaggerjwt.auth.dto.SignUpResponse;
import org.jjuni.swaggerjwt.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> signUp(@Validated @RequestBody SignUpRequest request) throws Exception {
        SignUpResponse response = authService.signUp(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 사용자 로그인
     *
     * - 우선 Access Token만 발급
     * @param request
     * @return
     */
    @Operation(summary = "로그인", description = "사용자 로그인 (jwt 토큰 발급)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Login Success")
    })
    @ResponseBody
    @PostMapping("sign-in")
    public ResponseEntity<?> signIn(@Validated @RequestBody SignInRequest request) {
        SignInResponse response = authService.signIn(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
