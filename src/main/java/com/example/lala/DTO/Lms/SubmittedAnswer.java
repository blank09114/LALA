package com.example.lala.DTO.Lms;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmittedAnswer {
    private String answer;
    private LocalDateTime submitDate;
}