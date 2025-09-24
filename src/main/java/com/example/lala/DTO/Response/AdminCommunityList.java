package com.example.lala.DTO.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCommunityList {

    private String userId;
    private String postId;
    private String writerNickname;
    private String contentTitle;
    private String accountType; // user_type
    private String postType;
    private LocalDateTime writeDate;
    private String reportType;

}
