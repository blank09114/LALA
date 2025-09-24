package com.example.lala.Controller;

import com.example.lala.Service.CommunityViewService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/auth/community")
public class CommunityViewController {

    private final CommunityViewService communityViewService;

    @GetMapping("/board")
    public String showCommunity(){

        return "community/board";
    }

    // 게시판 상세 페이지 이동
    @GetMapping("/boardDetails/{postId}")
    public String showCommunityDetail(@PathVariable String postId, Model model){
        System.out.println("postIdaa = " + postId); // 여기에 로그 찍히는지 확인

        return "community/boardDetails";
    }


    // 게시판 작성 페이지 이동
    @GetMapping("/boardWriter")
    public String creatCommunity(){

        return "community/writeBoard";
    }


}
