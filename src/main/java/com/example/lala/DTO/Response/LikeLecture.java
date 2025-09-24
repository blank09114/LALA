package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LikeLecture {
    private String lectureId;
    private String thumbnail;
    private String title;
    private String nickname;
    private String introduction;
    private Integer price;
    private Integer discountRate;
    private Integer reviewNum;
    private Double avgReviewRate;
    private Integer studentNum;
    private Integer difficulty;
    private String uploadDate;
}