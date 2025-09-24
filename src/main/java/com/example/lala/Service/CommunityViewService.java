package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Request.CommunityPostRequest;
import com.example.lala.DTO.Request.CommunityRequest;
import com.example.lala.DTO.Response.CommunityDetails;
import com.example.lala.DTO.Response.CommunityView;
import com.example.lala.Mapper.CommunityCommentMapper;
import com.example.lala.Mapper.CommunityLikeMapper;
import com.example.lala.Mapper.CommunityPostMapper;
import com.example.lala.Mapper.CommunityReportMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class CommunityViewService extends BaseService {

    private final CommunityReportMapper communityReportMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final CommunityLikeMapper communityLikeMapper;

    private final PermissionService permissionService;

    /**
     * 게시글 목록 조회
     */
    public Map<String, Object> findCommunityAll(List<Integer> postType, String title, String content, String writer, String orderBy, int page, int size) {
        int offset = (page - 1) * size;

        Map<String, Object> params = new HashMap<>();
        params.put("postType", postType);
        params.put("title", title);
        params.put("content", content);
        params.put("writer", writer);
        params.put("orderBy", orderBy);
        params.put("pageStart", offset);
        params.put("pageSize", size);

        List<CommunityView> communitys = communityPostMapper.findCommunityAll(params);
        int total = communityPostMapper.countCommunityAll(params);

        Map<String, Object> result = new HashMap<>();
        result.put("communitys", communitys);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return result;
    }

    /**
     * 게시글 상세 조회 (댓글 포함)
     */
    public Optional<CommunityView> getPostWithComments(String postId) {
        Optional<CommunityView> postOpt = communityPostMapper.findPostDetail(postId);

        if (postOpt.isPresent()) {
            CommunityView post = postOpt.get();

            // 조회수 증가
            communityPostMapper.incrementViewCount(postId);

            // 댓글 목록 조회
            List<CommunityDetails> flatComments = communityCommentMapper.findCommentsByPostId(postId);

            // 댓글 트리 구조 변환
            post.setComments(buildCommentTree(flatComments));

            return Optional.of(post);
        }
        return Optional.empty();
    }

    /**
     * 댓글 트리 구조 변환
     */
    private List<CommunityDetails> buildCommentTree(List<CommunityDetails> flatComments) {
        Map<String, CommunityDetails> commentMap = new HashMap<>();
        List<CommunityDetails> rootComments = new ArrayList<>();

        // 1. 모든 댓글을 맵에 저장하고 replies 초기화
        for (CommunityDetails comment : flatComments) {
            comment.setReplies(new ArrayList<>());
            commentMap.put(comment.getCommentId(), comment);
        }

        // 2. 부모 댓글에 자식 댓글 붙이기
        for (CommunityDetails comment : flatComments) {
            String parentId = comment.getParentCommentId();
            if (parentId == null || parentId.isEmpty()) {
                rootComments.add(comment);
            } else {
                CommunityDetails parent = commentMap.get(parentId);
                if (parent != null) {
                    parent.getReplies().add(comment);
                } else {
                    rootComments.add(comment); // 부모 없으면 루트로 처리
                }
            }
        }

        return rootComments;
    }

    /**
     * 댓글 등록
     */
    @Transactional
    public void setCreateComment(CommunityRequest comment) {
        String userId = getCurrentUserId();
        comment.setWriterId(userId);

        System.out.println("recommentCheck = " + comment.getRecommentCheck());

        if (comment.getRecommentCheck() == 0) {
            System.out.println(">> 일반 댓글 등록");
            communityCommentMapper.createComment(comment);
        } else if (comment.getRecommentCheck() == 1) {
            System.out.println(">> 대댓글 등록, parentCommentId = " + comment.getParentCommentId());
            communityCommentMapper.createReComment(comment);
        } else {
            throw new IllegalArgumentException("댓글 타입이 잘못되었습니다.");
        }
    }

    /**
     * 댓글 삭제 - PermissionService 사용
     */
    @Transactional
    public void deleteComment(String commentId) {
        System.out.println("서비스에서 댓글 삭제 시작: " + commentId);

        try {
            // 권한 확인
            if (!permissionService.canDeleteComment(commentId)) {
                throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
            }

            communityCommentMapper.deleteComment(commentId);
            System.out.println("댓글 삭제 완료");

            // 관리자 삭제인 경우 로그
            if (permissionService.isCurrentUserAdmin()) {
                String userId = getCurrentUserId();
                System.out.println("관리자에 의한 댓글 삭제 - 관리자: " + userId + ", 댓글ID: " + commentId);
            }

        } catch (Exception e) {
            System.out.println("댓글 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 좋아요 토글 (DB 기반)
     */
    @Transactional
    public Map<String, Object> toggleLike(String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = getCurrentUserId();
            System.out.println("좋아요 토글 - postId: " + postId + ", userId: " + userId);

            // 게시글 존재 확인
            if (!communityPostMapper.existsPostId(postId)) {
                result.put("success", false);
                result.put("message", "게시글을 찾을 수 없습니다.");
                return result;
            }

            // 사용자가 이미 좋아요를 눌렀는지 확인
            boolean isLiked = communityLikeMapper.checkUserLiked(postId, userId);
            System.out.println("현재 좋아요 상태: " + isLiked);

            if (isLiked) {
                // 좋아요 제거
                communityLikeMapper.removeLike(postId, userId);
                communityPostMapper.decrementLikeCount(postId);
                result.put("action", "removed");
                result.put("message", "좋아요를 취소했습니다.");
            } else {
                // 좋아요 추가
                communityLikeMapper.addLike(postId, userId);
                communityPostMapper.incrementLikeCount(postId);
                result.put("action", "added");
                result.put("message", "좋아요를 추가했습니다.");
            }

            // 업데이트된 좋아요 수 조회
            int currentLikeCount = communityPostMapper.getCurrentLikeCount(postId);

            result.put("success", true);
            result.put("likeCount", currentLikeCount);
            result.put("isLiked", !isLiked);

        } catch (Exception e) {
            System.out.println("좋아요 토글 중 오류: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 사용자의 좋아요 상태 확인 (DB 기반)
     */
    public boolean checkUserLiked(String postId) {
        try {
            String userId = getCurrentUserId();
            return communityLikeMapper.checkUserLiked(postId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 좋아요 수 조회
     */
    public int getCurrentLikeCount(String postId) {
        return communityPostMapper.getCurrentLikeCount(postId);
    }

    /**
     * 게시글 ID로 조회
     */
    public Optional<CommunityView> findPostId(String postId) {
        return communityPostMapper.findPostDetail(postId);
    }

    /**
     * 게시물 타입 상태 토글 (스터디: 모집중↔모집완료, 질문답변: 미해결↔해결)
     */
    @Transactional
    public Map<String, Object> togglePostTypeStatus(String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = getCurrentUserId();
            System.out.println("게시물 상태 변경 요청 - postId: " + postId + ", userId: " + userId);

            // 게시물 존재 확인
            if (!communityPostMapper.existsPostId(postId)) {
                result.put("success", false);
                result.put("message", "게시물을 찾을 수 없습니다.");
                return result;
            }

            // 게시물 작성자 확인
            if (!communityPostMapper.isPostOwner(postId, userId)) {
                result.put("success", false);
                result.put("message", "본인이 작성한 게시물만 상태를 변경할 수 있습니다.");
                return result;
            }

            // 현재 게시물 타입 조회
            int currentPostType = communityPostMapper.getCurrentPostType(postId);
            int newPostType;
            String statusMessage;

            switch (currentPostType) {
                case 3: // 스터디(모집중) → 스터디(모집완료)
                    newPostType = 4;
                    statusMessage = "모집이 완료되었습니다.";
                    break;
                case 4: // 스터디(모집완료) → 스터디(모집중)
                    newPostType = 3;
                    statusMessage = "모집을 다시 시작합니다.";
                    break;
                case 5: // 질문답변(미해결) → 질문답변(해결)
                    newPostType = 6;
                    statusMessage = "문제가 해결되었습니다.";
                    break;
                case 6: // 질문답변(해결) → 질문답변(미해결)
                    newPostType = 5;
                    statusMessage = "문제가 미해결 상태로 변경되었습니다.";
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "이 게시물은 상태를 변경할 수 없습니다.");
                    return result;
            }

            // 게시물 타입 업데이트
            int updatedRows = communityPostMapper.updatePostType(postId, newPostType);

            if (updatedRows > 0) {
                result.put("success", true);
                result.put("oldPostType", currentPostType);
                result.put("newPostType", newPostType);
                result.put("message", statusMessage);
            } else {
                result.put("success", false);
                result.put("message", "상태 변경에 실패했습니다.");
            }

        } catch (Exception e) {
            System.out.println("게시물 상태 변경 중 오류: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 게시물 상태 변경 가능 여부 확인
     */
    public boolean canTogglePostType(String postId) {
        try {
            String userId = getCurrentUserId();
            if (!communityPostMapper.isPostOwner(postId, userId)) {
                return false;
            }

            int currentPostType = communityPostMapper.getCurrentPostType(postId);
            return currentPostType == 3 || currentPostType == 4 || currentPostType == 5 || currentPostType == 6;
        } catch (Exception e) {
            return false;
        }
    }


    /*
    게시물 신고
     */
    @Transactional
    public Map<String, Object> reportPost (String postId, String content){

        Map<String, Object> result = new HashMap<>();

        try {
            // 회원 아이디 가져오기
            String userId = getCurrentUserId();

            // 게시물 존재 확인
            if (!communityPostMapper.existsPostId(postId)) {
                result.put("success", false);
                result.put("message", "존재하지 않는 게시물입니다.");
                return result;
            }

            // 이미 신고했는지 확인
            if (communityReportMapper.checkUserReport(postId, userId)) {
                result.put("success", false);
                result.put("message", "이미 신고한 게시물입니다.");
                return result;
            }

            // 신고 내용 유효성 검사
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "신고 사유를 입력해주세요");
                return result;
            }

            // 신고 추가
            int reportResult = communityReportMapper.addPostReport(postId, userId, content.trim());

            if (reportResult > 0) {

                // 게시물 신고 수 증가
                communityPostMapper.incrementReportCount(postId);
                result.put("success", true);
                result.put("message", "신고가 접수되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "신고 접수에 실패했습니다.");
            }

        }catch (Exception e){
            System.out.println("게시물 신고 처리 중 오류 " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "오류가 발생했습니다." + e.getMessage());
        }
        return result;

    }

    /**
     * 댓글 신고
     */
    @Transactional
    public Map<String, Object> reportComment(String commentId, String content) {
        Map<String, Object> result = new HashMap<>();

        try {
            String userId = getCurrentUserId();

            // 댓글 존재 확인
            if (!communityCommentMapper.existsCommentId(commentId)) {
                result.put("success", false);
                result.put("message", "존재하지 않는 댓글입니다.");
                return result;
            }

            // 이미 신고했는지 확인
            if (communityReportMapper.checkUserCommentReport(commentId, userId)) {
                result.put("success", false);
                result.put("message", "이미 신고한 댓글입니다.");
                return result;
            }

            // 신고 내용 유효성 검사
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "신고 사유를 입력해주세요.");
                return result;
            }

            // 신고 추가
            int reportResult = communityReportMapper.addCommentReport(commentId, userId, content.trim());

            if (reportResult > 0) {
                // 댓글 신고 수 증가
                communityCommentMapper.incrementReportCommentCount(commentId);
                result.put("success", true);
                result.put("message", "신고가 접수되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "신고 접수에 실패했습니다.");
            }

        } catch (Exception e) {
            System.out.println("댓글 신고 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 게시물 신고 여부 확인
     */
    public boolean checkPostReported(String postId) {
        try {
            String userId = getCurrentUserId();
            return communityReportMapper.checkUserReport(postId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 댓글 신고 여부 확인
     */
    public boolean checkCommentReported(String commentId) {
        try {
            String userId = getCurrentUserId();
            return communityReportMapper.checkUserCommentReport(commentId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    // 게시판 등록하기
    public void setCreatePost(CommunityPostRequest postRequest){
        String userId = getCurrentUserId();
        postRequest.setWriterId(userId);

        int posyType = postRequest.getPostTypeNum();

        permissionService.validateWritePermission(posyType);
        communityPostMapper.addPostUser(postRequest);
    }

    /**
     * 게시물 작성 가능 여부 확인 (UI에서 사용)
     */
    public boolean canUserWritePostType(int postType) {
        return permissionService.canWritePost(postType);
    }

    /**
     * 게시물 삭제 - 현재 테이블 구조 활용
     */
    @Transactional
    public void deletePost(String postId, String deleteReason) {
        System.out.println("서비스에서 게시물 삭제 시작: " + postId);

        try {
            // 권한 확인
            if (!permissionService.canDeletePost(postId)) {
                throw new IllegalArgumentException("게시물 삭제 권한이 없습니다.");
            }

            String finalDeleteReason = deleteReason;
            if (finalDeleteReason == null || finalDeleteReason.trim().isEmpty()) {
                boolean isAdmin = permissionService.isCurrentUserAdmin();
                finalDeleteReason = isAdmin ? "관리자에 의한 삭제" : "작성자에 의한 삭제";
            }

            // 현재 테이블 구조 활용: delete_check = 1, delete_reason 설정
            communityPostMapper.deletePost(postId, finalDeleteReason.trim());

            System.out.println("게시물 삭제 완료 - 사유: " + finalDeleteReason);

            // 관리자 삭제인 경우 로그
            if (permissionService.isCurrentUserAdmin()) {
                String userId = getCurrentUserId();
                System.out.println("관리자에 의한 게시물 삭제 - 관리자: " + userId + ", 게시물ID: " + postId);
            }

        } catch (Exception e) {
            System.out.println("게시물 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 게시물 삭제 권한 확인 (UI용)
     */
    public boolean canDeletePost(String postId) {
        return permissionService.canDeletePost(postId);
    }

    /**
     * 삭제 권한 상세 정보 조회
     */
    public Map<String, Object> getDeletePermissionInfo(String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean canDelete = permissionService.canDeletePost(postId);
            result.put("success", true);
            result.put("canDelete", canDelete);

            if (canDelete) {
                String userId = getCurrentUserId();
                boolean isOwner = communityPostMapper.isPostOwner(postId, userId);
                boolean isAdmin = permissionService.isCurrentUserAdmin();

                result.put("isOwner", isOwner);
                result.put("isAdmin", isAdmin);
                result.put("deleteType", isAdmin && !isOwner ? "admin" : "owner");
            }

            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "권한 확인 중 오류가 발생했습니다.");
            return result;
        }
    }


    }



