package com.example.lala.API;

import com.example.lala.DTO.Request.CommunityPostRequest;
import com.example.lala.DTO.Request.CommunityRequest;
import com.example.lala.DTO.Response.CommunityView;
import com.example.lala.Service.CommunityViewService;
import com.example.lala.Service.PermissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/community")

public class CommunityViewAPI {

    private final CommunityViewService communityViewService;
    private final PermissionService permissionService;


    /**
     * 게시글 목록 조회
     */
    @GetMapping("/board")
    @PreAuthorize("permitAll()")
    public Map<String, Object> showCommunityView(@RequestParam(required = false) List<Integer> postType,
                                                 @RequestParam(required = false) String title,
                                                 @RequestParam(required = false) String content,
                                                 @RequestParam(required = false) String writer,
                                                 @RequestParam(required = false) String orderBy,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return communityViewService.findCommunityAll(postType, title, content, writer, orderBy, page, size);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/boardDetails/{postId}")
    public ResponseEntity<CommunityView> showCommunityDetail(@PathVariable String postId) {
        System.out.println("====================================");
        System.out.println("🔥 API 호출됨! postId = " + postId);
        System.out.println("====================================");

        try {
            Optional<CommunityView> result = communityViewService.getPostWithComments(postId);

            if (result.isPresent()) {
                System.out.println("✅ 데이터 조회 성공: " + result.get().getPostTitle());
                return ResponseEntity.ok(result.get());
            } else {
                System.out.println("❌ 데이터 없음 - 404 반환");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.out.println("💥 API 오류 발생:");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // 게시물 등록

    @PostMapping("/board/post")
    public ResponseEntity<String> createPost(@RequestBody CommunityPostRequest postRequest){
        System.out.println("게시물 등록 요청 + writerId" + postRequest.getWriterId());
        System.out.println("게시물 내용" + postRequest.getContent());

        try{
            communityViewService.setCreatePost(postRequest);
            return ResponseEntity.ok("게시물 등록 완료");
        }catch (SecurityException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("권한 오류 " + e.getMessage());
        } catch (Exception e){
            System.out.println("게시물 등록 오류 " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시물 작성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 게시물 삭제 권한 확인
     */
    @GetMapping("/boardDetails/{postId}/can-delete")
    public ResponseEntity<Map<String, Object>> canDeletePost(@PathVariable String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> deleteInfo = communityViewService.getDeletePermissionInfo(postId);
            return ResponseEntity.ok(deleteInfo);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "권한 확인에 실패했습니다.");
            return ResponseEntity.badRequest().body(result);
        }
    }



    /**
     * 게시물 삭제 (현재 테이블 구조 활용)
     */
    @DeleteMapping("/boardDetails/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable String postId,
                                             @RequestBody(required = false) Map<String, String> request) {
        System.out.println("게시물 삭제 요청: " + postId);

        try {
            String deleteReason = null;

            // 요청 본문에서 삭제 사유 추출
            if (request != null && request.containsKey("deleteReason")) {
                deleteReason = request.get("deleteReason");
                System.out.println("삭제 사유: " + deleteReason);
            }

            // 삭제 사유 유효성 검사
            if (deleteReason == null || deleteReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("삭제 사유를 입력해주세요.");
            }

            if (deleteReason.trim().length() < 5) {
                return ResponseEntity.badRequest().body("삭제 사유를 5자 이상 입력해주세요.");
            }

            // delete_reason 컬럼 크기 제한 (100자)
            if (deleteReason.trim().length() > 100) {
                return ResponseEntity.badRequest().body("삭제 사유는 100자 이내로 입력해주세요.");
            }

            communityViewService.deletePost(postId, deleteReason.trim());
            return ResponseEntity.ok("게시물 삭제 완료");

        } catch (IllegalArgumentException e) {
            System.out.println("권한 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한 오류: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패: " + e.getMessage());
        }
    }



    /**
     * 댓글 등록
     */
    @PostMapping("/boardDetails")
    public ResponseEntity<String> createComment(@RequestBody CommunityRequest request) {
        System.out.println("댓글 등록 요청 - postId: " + request.getPostId());
        System.out.println("댓글 내용: " + request.getContent());

        try {
            communityViewService.setCreateComment(request);
            return ResponseEntity.ok("댓글 등록 완료");
        } catch (Exception e) {
            System.out.println("댓글 등록 오류: " + e.getMessage());
            return ResponseEntity.status(500).body("댓글 등록 실패: " + e.getMessage());
        }
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        System.out.println("삭제 요청 받음: " + commentId);

        try {
            communityViewService.deleteComment(commentId);
            return ResponseEntity.ok("댓글 삭제 완료");
        } catch (Exception e) {
            System.out.println("삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    /**
     * 좋아요 토글 (DB 기반)
     */
    @PostMapping("/boardDetails/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable String postId) {
        System.out.println("좋아요 토글 요청 - postId: " + postId);

        Map<String, Object> result = communityViewService.toggleLike(postId);

        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 좋아요 상태 확인 (DB 기반)
     */
    @GetMapping("/boardDetails/{postId}/like-status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean isLiked = communityViewService.checkUserLiked(postId);
            int likeCount = communityViewService.getCurrentLikeCount(postId);

            result.put("success", true);
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "상태 조회에 실패했습니다.");
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 게시물 타입 상태 토글 (스터디: 모집중↔모집완료, 질문답변: 미해결↔해결)
     */
    @PostMapping("/boardDetails/{postId}/toggle-status")
    public ResponseEntity<Map<String, Object>> togglePostTypeStatus(@PathVariable String postId) {
        System.out.println("게시물 상태 토글 요청 - postId: " + postId);

        Map<String, Object> result = communityViewService.togglePostTypeStatus(postId);

        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 게시물 상태 변경 가능 여부 확인
     */
    @GetMapping("/boardDetails/{postId}/can-toggle-status")
    public ResponseEntity<Map<String, Object>> canToggleStatus(@PathVariable String postId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean canToggle = communityViewService.canTogglePostType(postId);
            result.put("success", true);
            result.put("canToggle", canToggle);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "상태 확인에 실패했습니다.");
            return ResponseEntity.badRequest().body(result);
        }
    }


    // 게시물 신고

    @PostMapping("/boardDetails/{postId}/report")
    public ResponseEntity<Map<String, Object>> reportPost(@PathVariable String postId,
                                                          @RequestBody Map<String, String> request){

        System.out.println("게시물 신고 요청 postId = " + postId);

        String content = request.get("content");
        Map<String, Object> result = communityViewService.reportPost(postId, content);

        if((Boolean) result.get("success")){
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body(result);
        }
    }

    // 댓글 신고
    @PostMapping("/comment/{commentId}/report")
    public ResponseEntity<Map<String, Object>> reportComment(@PathVariable String commentId,
                                                             @RequestBody Map<String, String> request) {
        System.out.println("댓글 신고 요청 - commentId: " + commentId);

        String content = request.get("content");
        Map<String, Object> result = communityViewService.reportComment(commentId, content);

        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }


    /**
            * 사용자 권한 확인 API
    */
    @GetMapping("/user/permission")
    public ResponseEntity<Map<String, Object>> getUserPermission() {
        Map<String, Object> result = new HashMap<>();

        try {
            PermissionService.UserPermissionInfo userInfo = permissionService.getCurrentUserPermissions();

            result.put("success", true);
            result.put("isAdmin", userInfo.isAdmin());
            result.put("userId", userInfo.getUserId());
            result.put("userType", userInfo.getUserType());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("isAdmin", false);
            result.put("message", "권한 확인 중 오류가 발생했습니다.");
            return ResponseEntity.ok(result);
        }
    }


}
