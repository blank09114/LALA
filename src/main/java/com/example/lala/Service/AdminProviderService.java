package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Response.AdminProviderRequest;
import com.example.lala.Mapper.AdminProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 지식제공자 요청 관리 서비스
 * 지식제공자 요청 조회, 승인 처리 등의 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProviderService extends BaseService {

    private final AdminProviderMapper adminProviderMapper;

    /**
     * 특정 사용자의 지식제공자 요청 정보를 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 지식제공자 요청 정보
     */
    public Optional<AdminProviderRequest> findProviderRequest(String userId) {
        try {
            log.info("지식제공자 요청 정보 조회 시작 - userId: {}", userId);

            AdminProviderRequest request = adminProviderMapper.findProviderRequest(userId);

            if (request != null) {
                log.info("지식제공자 요청 정보 조회 성공 - userId: {}, nickname: {}",
                        userId, request.getNickname());
                return Optional.of(request);
            } else {
                log.warn("지식제공자 요청 정보를 찾을 수 없음 - userId: {}", userId);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("지식제공자 요청 정보 조회 중 오류 발생 - userId: {}, error: {}",
                    userId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 지식제공자 요청을 승인 처리
     * promotion_request 테이블의 approval_date를 현재 시간으로 업데이트
     *
     * @param userId 승인할 사용자 ID
     */
    @Transactional
    public void approveProviderRequest(String userId) {
        try {
            log.info("지식제공자 요청 승인 처리 시작 - userId: {}", userId);

            LocalDateTime approvalDate = LocalDateTime.now();
            int updatedRows = adminProviderMapper.updateApprovalDate(userId, approvalDate);

            if (updatedRows > 0) {
                log.info("지식제공자 요청 승인 완료 - userId: {}, approvalDate: {}",
                        userId, approvalDate);
            } else {
                log.warn("승인할 지식제공자 요청을 찾을 수 없음 - userId: {}", userId);
                throw new RuntimeException("승인할 요청을 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            log.error("지식제공자 요청 승인 처리 중 오류 발생 - userId: {}, error: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("승인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 지식제공자 요청을 거부 처리
     * promotion_request 테이블에서 해당 요청을 삭제하거나 상태를 변경
     *
     * @param userId 거부할 사용자 ID
     */
    @Transactional
    public void rejectProviderRequest(String userId) {
        try {
            log.info("지식제공자 요청 거부 처리 시작 - userId: {}", userId);

            // 요청을 삭제하거나 거부 상태로 변경
            int deletedRows = adminProviderMapper.deleteProviderRequest(userId);

            if (deletedRows > 0) {
                log.info("지식제공자 요청 거부 완료 - userId: {}", userId);
            } else {
                log.warn("거부할 지식제공자 요청을 찾을 수 없음 - userId: {}", userId);
                throw new RuntimeException("거부할 요청을 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            log.error("지식제공자 요청 거부 처리 중 오류 발생 - userId: {}, error: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("거부 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}