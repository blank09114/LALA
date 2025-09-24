package com.example.lala.API;

import com.example.lala.DTO.Auth.CustomUserDetailsImpl;
import com.example.lala.Service.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users/mypage")
public class LikeAPI {

    @Autowired
    private LikeService likeService;

    @DeleteMapping("/unlike-lecture/{lectureId}")
    public ResponseEntity<Void> unlikeLecture(@PathVariable String lectureId, Authentication authentication) {
        CustomUserDetailsImpl userDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getUserId();
        System.out.println("✅ Authentication 기반 user_id: " + userId);
        likeService.unlikeLecture(userId, lectureId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/unlike-provider/{providerId}")
    public ResponseEntity<Void> unlikeProvider(@PathVariable String providerId, Principal principal) {
        String userId = principal.getName();
        likeService.unlikeProvider(userId, providerId);
        return ResponseEntity.ok().build();
    }
}

