package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureUpdateRequest {
    private String lectureId;
    private String thumbnail;
    private String name;
    private String outline;
    private String introduction;
    private Integer price;
    private Integer discountRate;
    private Integer difficulty;
}
