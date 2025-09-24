package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReview {
    private String reviewId;      // 수정할 리뷰 ID
    private String writerId;      // 작성자 ID (보안 검증용)
    private int reviewRate;       // 수정할 평점
    private String content;       // 수정할 내용
}