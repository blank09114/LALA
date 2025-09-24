package com.example.lala.DTO.Response;

import lombok.Data;

@Data
public class IPLecture {
    private String lectureId;
    private String thumbnail;
    private String title;
    private int price;
    private double discountRate;
    private double reviewAVG;
    private int reviewSum;
    private int studentNum;
}
