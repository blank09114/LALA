package com.example.lala.DTO.Lms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegQna {
    private String lectureContentId;
    private String questionalId;
    private String questionContent;
}
