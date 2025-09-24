package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyCommunity {
    private String postId;
    private Integer postType;
    private String title;
    private Boolean recomment;
    private Boolean contentCheck;
    private String content;
    private String date;
    private String nickname;
    private Integer like;
    private Integer view;
    private Integer comment;
    private Boolean deleted;
    private String deleteReason;
}