package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegMaterial {
    private String materialId;  // 생성된 Material ID를 받을 필드
    private int type;           // 자료 타입 (1=동영상, 2=추가자료, 3=퀴즈)
}
