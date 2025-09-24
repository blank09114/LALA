package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LectureSummary {
    private String lectureId;
    private String thumbnail;
    private String title;
    private String nickname;
    private String introduction;
    private int price;
    private int discountRate;
    private int reviewNum;
    private int totalReviewRate;
    private Double avgReviewRate;
    private int bookmarkNum;
    private int difficulty;
}