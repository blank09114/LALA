package com.example.lala.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IPInfo {
    private String nickname;
    private String profilename;
    private String infoContent;
    private String externalLink;
    private Integer reviewNum;
    private Integer studentNum;
    private Double reviewAvg;
}
