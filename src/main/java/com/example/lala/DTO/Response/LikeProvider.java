package com.example.lala.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LikeProvider {
    private String providerId;
    private String profileName;
    private String nickname;
    private String infoContent;
    private double avgReviewRate;
    private Integer reviewNum;
    private Integer bookmarkNum;
    private Integer lectureNum;
}
