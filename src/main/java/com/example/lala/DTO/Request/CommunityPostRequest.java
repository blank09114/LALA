package com.example.lala.DTO.Request;

import lombok.Data;

@Data
public class CommunityPostRequest {

    private String writerId;
    private int postTypeNum;
    private String title;
    private String content;

}
