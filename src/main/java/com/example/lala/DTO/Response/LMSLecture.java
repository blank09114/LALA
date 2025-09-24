package com.example.lala.DTO.Response;

import lombok.Data;

@Data
public class LMSLecture {
    private float progressRate;
    private String name;
    private String thumbnail;
    private String interests;
    private String nickname;
    private String lectureTitle;
    private String outline;
    private Integer bookmark;
    private Integer students;
    private Integer status;
}
