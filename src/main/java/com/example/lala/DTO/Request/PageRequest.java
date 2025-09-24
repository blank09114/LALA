package com.example.lala.DTO.Request;

import lombok.Data;

@Data
public class PageRequest {
    private int page = 1;           // 현재 페이지 번호 (1부터 시작)
    private int size = 10;          // 페이지당 항목 수
    private int offset;             // 데이터베이스 OFFSET 값

    public PageRequest() {
    }

    public PageRequest(int page, int size) {
        this.page = Math.max(1, page);
        this.size = Math.max(1, size);
        this.offset = (this.page - 1) * this.size;
    }

    public int getOffset() {
        return (this.page - 1) * this.size;
    }
}
