package com.example.lala.DTO.Response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommunityDetails {

    private String postId;
    private String commentId;
    private String parentCommentId; // 부모 댓글 ID (null이면 최상위 댓글)

    private String writerNickname;
    private String content;
    private String contentType; // 댓글 / 대댓글
    private String writeDate;
    private int likeNum;
    private int commentNum;

    private boolean recommendCheck;
    private boolean deleteCheck;
    private boolean report;

    // 대댓글 리스트 (트리 구조)
    private List<CommunityDetails> replies;

    // Builder 사용 시 replies 초기화
    public static class CommunityDetailsBuilder {
        private List<CommunityDetails> replies = new ArrayList<>();
    }

    private List<CommunityDetails> comments;


}