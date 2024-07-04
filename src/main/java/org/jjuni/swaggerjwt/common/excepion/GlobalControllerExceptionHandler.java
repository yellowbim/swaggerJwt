package org.jjuni.swaggerjwt.common.excepion;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jjuni.swaggerjwt.common.dto.CommResponse;
import org.jjuni.swaggerjwt.common.enums.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerExceptionHandler extends Exception {
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        BindingResult bindingResult = ex.getBindingResult();
        CommResponse<?> response = CommResponse.createFail(bindingResult);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle JWT Exception Handler
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<CommResponse<?>> handleJwtExceptions(JwtException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        CommResponse<?> response = CommResponse.createError(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Handle JWT Access Token Expired Handler
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CommResponse<?>> handleExpiredJwtExceptions(ExpiredJwtException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        CommResponse<?> response;
        // Access, Refresh 두 다 ExpiredJwtException 으로 던져서 Refresh Token 에러 발생하는 부분으로 잡아서 판단....
        // 내키지 않지만 현재 뚜렷한 방법이 떠오르지 않음...
        if (ex.getMessage().contains("Refresh Token Expired")) {
            response = CommResponse.createError(ResultCode.JWT_REFRESH_TOKEN_EXPIRED.getResultMessage());
        } else {
            response = CommResponse.createError(ResultCode.JWT_ACCESS_TOKEN_EXPIRED.getResultMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommResponse<?>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        CommResponse<?> response = CommResponse.createError(ResultCode.INTERNAL_ERROR.getResultMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
