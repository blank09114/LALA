package com.example.lala.DTO.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 게시글 조회를 위한 DTO 클래스
 * 게시글 작성자 정보와 게시글 기본 정보를 포함합니다.
 */
@Data
public class AdminUserActivity {

   private String contentId; // 게시글, 댓글, 강의평을 식별하는 id
    private String contentType; // post, comment, recomment, review
    private String contentName; // 게시물 타입 (자유, 질문답변, 스터디)
    private String contentTitle; // 게시글 제목이나 강의명
    private String contentText; // 내용
    private LocalDateTime writeDate; // 게시물
    private String writerNickname; // 작성자 닉네임
    private Integer userType;

    private String reportsJson;
    // 신고
    private boolean isReported; // true면 신고당한 글
    private boolean isReportedByMe; // true면 내가 신고한 글
    private List<ReportDetail> reports; // 신고 상세 내용

}