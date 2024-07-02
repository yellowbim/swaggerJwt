package org.jjuni.swaggerjwt.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommResponse<T> {
    private static final String SUCCESS_STATUS = "success";
    private static final String FAIL_STATUS = "fail";
    private static final String ERROR_STATUS = "error";

    private String status;
    private T data;
    private String message;

    public static <T> CommResponse<T> createSuccess(T data) {
        return new CommResponse<>(SUCCESS_STATUS, data, null);
    }

    public static CommResponse<?> createSuccessWithNoContent() {
        return new CommResponse<>(SUCCESS_STATUS, null, null);
    }

    // Hibernate Validator에 의해 유효하지 않은 데이터로 인해 API 호출이 거부될때 반환
    public static CommResponse<?> createFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError) {
                errors.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errors.put( error.getObjectName(), error.getDefaultMessage());
            }
        }
        return new CommResponse<>(FAIL_STATUS, errors, null);
    }

    // 예외 발생으로 API 호출 실패시 반환
    public static CommResponse<?> createError(String message) {
        return new CommResponse<>(ERROR_STATUS, null, message);
    }

    private CommResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

}
