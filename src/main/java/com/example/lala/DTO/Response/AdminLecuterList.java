package com.example.lala.DTO.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminLecuterList {

    private String userId;
    private String lectureId;
    private String lectureNickname; // 강의 요청자 닉네임
    private String requestType; // 요청 타입 (등록, 수정)
    private String contentName;
    private LocalDateTime uploadDate; // 요청일 (업로드 날짜)
    private String statusName; // 0. 등록거부, 1. 승인, 2. 요청중
}
