package com.example.lala.DTO.Course;

import lombok.Data;

import java.util.Date;

@Data
public class Review {
    private String reviewId;
    private String writerId;
    private String profilename;
    private String nickname;
    private int reviewRate;
    private Date writeDate;
    private String content;
    private String thumbnail;
    private String avgReviewRate;
}
