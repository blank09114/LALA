package com.example.lala.DTO.Lms;

import lombok.Data;

import java.util.Date;

@Data
public class AddiDetail {
    private String matId;
    private String conId;
    private String name;             // 추가자료 파일 이름
    private String address;          // 파일 주소 (경로 또는 URL)
    private String format;           // 파일 형식 (pdf, docx 등)
    private String comment;
}