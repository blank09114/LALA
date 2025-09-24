package com.example.lala.DTO.Course;

import lombok.Data;

@Data
public class RegReview {
    String lectureId;
    String writerId;
    int reviewRate;
    String content;
}
