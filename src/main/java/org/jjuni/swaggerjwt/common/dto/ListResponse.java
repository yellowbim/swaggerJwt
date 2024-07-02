package org.jjuni.swaggerjwt.common.dto;

import lombok.Data;

@Data
public class ListResponse<T> {
    private T list;
}
