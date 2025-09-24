package com.example.lala.DTO.Request;

import lombok.Data;

@Data
public class CommunityRequest {

    private String postId;
    private String writerId;
    private String content;
    private int recommentCheck;
    private String parentCommentId; // 유지!
    private String commentId;
}
