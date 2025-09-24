package com.example.lala.DTO;

import lombok.Data;

@Data
public class AdminTodayStatisticsDTO {

    private int userCountToday; // 오늘 가입자 수
    private int lectureRequestCountToday; // 강의 요청 수
    private int providerApplyCountToday; //  지식제공자 요청 수
    private int postCountToday; // 게시글 업로드 수
    private int reportedPostCountToday; // 신고된 게시물 수

}
