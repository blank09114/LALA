package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import com.example.lala.DTO.Oauth.CustomOAuth2UserImpl;
import com.example.lala.Mapper.CommunityCommentMapper;
import com.example.lala.Mapper.CommunityPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService extends BaseService {

    private final CommunityPostMapper communityPostMapper;
    private final CommunityCommentMapper communityCommentMapper;

    // 관리자 권한 확인 클래스

    // 관리자 확인
    public boolean isCurrentUserAdmin(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || !authentication.isAuthenticated()){
                return false;
            }
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        }catch (Exception e){
            System.out.println("관리자 권한 확인 중 오류 : " + e.getMessage());
            return false;
        }
    }

    // 게시물 삭제 권한 확인
    public boolean canDeletePost(String postId){
        try{
            String userId = getCurrentUserId();

            // 게시물 존재 확인
            if(!communityPostMapper.existsPostId(postId)){
                return false;
            }

            // 작성자 확인
            boolean isOwner = communityPostMapper.isPostOwner(postId, userId);

            // 관리자 확인
            boolean isAdmin = isCurrentUserAdmin();

            return isOwner || isAdmin;
        }catch (Exception e){
            System.out.println("게시물 삭제 권한 확인 중 오류 : " + e.getMessage());
            return false;
        }
    }

    // 댓글 권한 확인
    public boolean canDeleteComment(String commentId) {
        try {
            String userId = getCurrentUserId();

            // 댓글 존재 확인
            if (!communityCommentMapper.existsCommentId(commentId)) {
                return false;
            }

            // 작성자 확인
            boolean isOwner = communityCommentMapper.isCommentOwner(commentId, userId);

            // 관리자 확인
            boolean isAdmin = isCurrentUserAdmin();

            return isOwner || isAdmin;
        } catch (Exception e) {
            System.out.println("댓글 삭제 권한 확인 중 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * 게시물 수정 권한 확인
     */
    public boolean canEditPost(String postId) {
        try {
            String userId = getCurrentUserId();

            // 게시물 존재 확인
            if (!communityPostMapper.existsPostId(postId)) {
                return false;
            }

            // 작성자만 수정 가능 (관리자는 수정 불가, 삭제만 가능)
            return communityPostMapper.isPostOwner(postId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 댓글 수정 권한 확인
     */
    public boolean canEditComment(String commentId) {
        try {
            String userId = getCurrentUserId();

            // 댓글 존재 확인
            if (!communityCommentMapper.existsCommentId(commentId)) {
                return false;
            }

            // 작성자만 수정 가능 (관리자는 수정 불가, 삭제만 가능)
            return communityCommentMapper.isCommentOwner(commentId, userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 게시물 상태 변경 권한 확인 (스터디 모집상태, 질문답변 해결상태)
     */
    public boolean canTogglePostStatus(String postId) {
        try {
            String userId = getCurrentUserId();

            // 게시물 존재 확인
            if (!communityPostMapper.existsPostId(postId)) {
                return false;
            }

            // 작성자만 상태 변경 가능
            if (!communityPostMapper.isPostOwner(postId, userId)) {
                return false;
            }

            // 상태 변경 가능한 게시물 타입인지 확인
            int currentPostType = communityPostMapper.getCurrentPostType(postId);
            return currentPostType == 3 || currentPostType == 4 || currentPostType == 5 || currentPostType == 6;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 사용자의 권한 정보를 반환
     */
    public UserPermissionInfo getCurrentUserPermissions() {
        try {
            String userId = getCurrentUserId();
            boolean isAdmin = isCurrentUserAdmin();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userType = "UNKNOWN";

            if (authentication.getPrincipal() instanceof CustomUserDetailsImpl) {
                userType = "GENERAL";
            } else if (authentication.getPrincipal() instanceof CustomOAuth2UserImpl) {
                userType = "OAUTH2";
            }

            return new UserPermissionInfo(userId, isAdmin, userType);
        } catch (Exception e) {
            return new UserPermissionInfo(null, false, "UNKNOWN");
        }
    }

    /**
     * 사용자 권한 정보 클래스
     */
    public static class UserPermissionInfo {
        private final String userId;
        private final boolean isAdmin;
        private final String userType;

        public UserPermissionInfo(String userId, boolean isAdmin, String userType) {
            this.userId = userId;
            this.isAdmin = isAdmin;
            this.userType = userType;
        }

        public String getUserId() { return userId; }
        public boolean isAdmin() { return isAdmin; }
        public String getUserType() { return userType; }
    }

    // 게시물 작성 권한 확인
    public boolean canWritePost(int postType){
        // 관리자 확인
        try{
           if(postType == 1){
               return isCurrentUserAdmin();
           }

           String userId = getCurrentUserId();
           return userId != null && !userId.isEmpty();

        }catch (Exception e){
            System.out.println("게시물 작성 권한 확인 중 오류 : " + e.getMessage());
            return false;
        }
    }

    // 특정 게시물 타입이 관리자 전용인지 확인
    public boolean isAdminOnlyPostType (int postType){
        return postType == 1;
    }


    // 게시물 작성 전 권한 검증 (예외 발생)
    public void validateWritePermission(int postType){
        // postType
        if(!canWritePost(postType)){
           if(postType == 1){
               throw new SecurityException("공지사항은 관리자만 작성할 수 있습니다.");
           }else {
               throw new SecurityException("게시물 작성 권한이 없습니다.");
           }
        }
    }


}


