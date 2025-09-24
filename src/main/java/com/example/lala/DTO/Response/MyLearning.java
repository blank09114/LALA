package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyLearning {
    private String lectureId;
    private String thumbnail;
    private String title;
    private String nickname;
    private String introduction;
    private float progressRate;
    private boolean hasReview; // 수강평 작성 여부
}
