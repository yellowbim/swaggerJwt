package org.jjuni.swaggerjwt.common.dto;

public class PageResponse<T> {
    private T list;
    private int page;
    private int size;
    private Long totalCount;
    private int lastPage;

    public PageResponse(int page, int size) {
        this.page = page;
        this.size = size;
    }
}
