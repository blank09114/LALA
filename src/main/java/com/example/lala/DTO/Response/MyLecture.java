package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyLecture {
    private String id;
    private String title;
    private String provider;
    private String thumbnail;
    private String last_update_date;
}