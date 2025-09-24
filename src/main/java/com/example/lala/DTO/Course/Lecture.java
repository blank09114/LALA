package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Lecture {
    private String lectureId;
    private String providerId;
    private String thumbnail;
    private String title;
    private String nickname;
    private String outline;
    private String introduction;
    private int price;
    private int discountRate;
    private int reviewNum;
    private Double avgReviewRate;
    private int studentNum;
    private int bookmarkNum;
    private int difficulty;
    private String big;
    private String small;
}