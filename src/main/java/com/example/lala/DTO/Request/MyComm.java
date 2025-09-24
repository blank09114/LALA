package com.example.lala.DTO.Request;

import lombok.Data;

@Data
public class MyComm {
    private Integer[] postType;
    private Boolean contentCheck;
    private Boolean deleted;
    private PageRequest pageRequest;

    public MyComm() {
        this.pageRequest = new PageRequest(1, 10); // 기본값
    }
}