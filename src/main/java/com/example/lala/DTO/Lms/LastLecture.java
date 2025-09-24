package com.example.lala.DTO.Lms;

import lombok.Data;

@Data
public class LastLecture {
    private String lectureId;        // 강의 ID
    private String thumbnail;        // 썸네일 경로
    private String title;            // 강의명
    private String nickname;         // 강사 닉네임
    private int userType;            // 강사 유형 (ex. 관리자, 일반, 강사 등)
    private String outline;          // 강의 개요
    private String introduction;     // 강의 소개
    private int price;               // 가격
    private int discountRate;        // 할인율
    private int reviewNum;           // 리뷰 수
    private float avgReviewRate;     // 평균 평점
    private int studentNum;          // 수강생 수
    private int difficulty;          // 난이도
    private String lectureStatus;    // 강의 상태 (승인 대기, 승인, 거부 등)
    private int bookmarkNum;         // 찜 수
    private Float progressRate;      // 수강 진도율
    private String big;              // 대분류
    private String small;            // 소분류들 (콤마 구분 문자열)
}
