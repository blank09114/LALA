package com.example.lala.DTO.Lms;

import lombok.Data;

@Data
public class Question {
    private String question;
    private String matId;
    private String conId;
    private String questionId;
    private String choiceAnswer;
    private String type;
    private String answer;
    private String comment;
}
