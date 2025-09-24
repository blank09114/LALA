package com.example.lala.DTO.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityView {

    private String postId;
    private String postType; // 공지 / 자유 / 스터디 / 질문답변
    private String postTypeName; // 모집중 , 모집완료 / 해결 , 미해결
    private String postTitle; // 게시물 제목
    private String postContent; // 게시물 내용
    private String deleteReason;
    private String writerNickname; // 작성자 닉네임
    private String writerId;  // 추가
    private LocalDateTime writeDate; // 작성일
    private int postView; // 뷰 수
    private int postLikeNum; // 좋아요 수
    private int postCommNum; // 댓글 수

    // 댓글 정보
    private List<CommunityDetails> comments;



}
