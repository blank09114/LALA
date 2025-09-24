package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Review {
    private String reviewId;
    private String thumbnail;
    private Integer rate;
    private String nickname;
    private String writeDate;
    private String lectureTitle;
    private String content;
}
