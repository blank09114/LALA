package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegLecture {
    private String lectureId;      // 생성된 ID를 받을 필드 추가
    private String uploaderId;
    private String thumbnail;
    private String name;
    private String outline;
    private String introduction;
    private int price;
    private int discountRate;
    private Integer difficulty;
}
