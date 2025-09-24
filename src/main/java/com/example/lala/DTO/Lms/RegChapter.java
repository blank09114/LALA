package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegChapter {
    private String chapterId;      // 생성된 목차 ID를 받을 필드 추가
    private String lectureId;
    private String name;
    private String objective;
    private int orderNum;
}
