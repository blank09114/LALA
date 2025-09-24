package com.example.lala.DTO.Response;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;        // 현재 페이지의 데이터
    private int currentPage;        // 현재 페이지 번호
    private int pageSize;           // 페이지당 항목 수
    private long totalElements;     // 전체 항목 수
    private int totalPages;         // 전체 페이지 수
    private boolean hasNext;        // 다음 페이지 존재 여부
    private boolean hasPrevious;    // 이전 페이지 존재 여부

    public PageResponse(List<T> content, int currentPage, int pageSize, long totalElements) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
    }
}
