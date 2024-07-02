package org.jjuni.swaggerjwt.common.dto;

import lombok.Data;

@Data
public class DataResponse<T> {
    private T data;
}
