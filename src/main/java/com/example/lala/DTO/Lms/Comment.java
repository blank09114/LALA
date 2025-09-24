package com.example.lala.DTO.Lms;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private String nickname;
    private String profilename;
    private Date questionDate;
    private String questionContent;
    private String answerContent;
    private Date answerDate;
}
