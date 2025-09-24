package com.example.lala.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 📝 지식 제공자 신청 요청 DTO
 *
 * 학력/약력과 첨부파일을 포함한 지식 제공자 신청 정보를 담습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProReqRequest {

    // 📋 기본 정보
    private String name;           // 실명
    private String nickname;       // 활동명
    private String intro;          // 소개
    private String externallink;   // 외부 링크

    // 📚 학력/약력 정보 리스트
    private List<HistoryItem> history;

    /**
     * 📖 학력/약력 항목 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        private String text;                    // 학력/약력 텍스트
        private MultipartFile file;             // 첨부파일 (선택사항)
        private String fileUrl;                 // 업로드된 파일 URL (서버에서 설정)
    }
}