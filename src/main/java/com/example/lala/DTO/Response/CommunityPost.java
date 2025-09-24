package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 커뮤니티 게시글 정보를 담는 DTO 클래스
 * 게시글의 기본 정보와 통계 정보를 포함합니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommunityPost {
    private String postId;
    private int postType;
    private String title;
    private String content;
    private String date;
    private String nickname;
    private int likeCount;
    private int viewCount;
    private int commentCount;
    private boolean deleted;
}