package com.example.lala.DTO.Response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CancelLecture {
    private String lectureId;
    private String thumbnail;
    private String title;
    private String nickname;
    private Integer price;
    private String refundDate;
    private Integer difficulty;
}
