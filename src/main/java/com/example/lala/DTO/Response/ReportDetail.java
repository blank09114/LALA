package com.example.lala.DTO.Response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportDetail {

    private String reportNickname;
    private String reason;
    private LocalDate reportDate;
    // 신고 날짜??
}
