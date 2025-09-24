package com.example.lala.DTO.Lms;

import lombok.Data;

@Data
public class QuestionDetail {
    private String questionId;
    private String conId;
    private String question;
    private String choiceAnswer;
    private String answer;
    private String type;
    private String comment;
    private String matId;
}
