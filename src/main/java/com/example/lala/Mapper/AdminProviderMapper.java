package com.example.lala.Mapper;

import com.example.lala.DTO.Response.AdminProviderRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 지식제공자 요청 관련 데이터베이스 접근 매퍼
 */
@Mapper
public interface AdminProviderMapper {

    /**
     * 특정 사용자의 지식제공자 요청 정보를 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 지식제공자 요청 정보
     */
    AdminProviderRequest findProviderRequest(@Param("userId") String userId);

    /**
     * 지식제공자 요청의 승인 일시를 업데이트
     *
     * @param userId 승인할 사용자 ID
     * @param approvalDate 승인 일시
     * @return 업데이트된 행 수
     */
    int updateApprovalDate(@Param("userId") String userId,
                           @Param("approvalDate") LocalDateTime approvalDate);

    /**
     * 지식제공자 요청을 삭제 (거부 처리)
     *
     * @param userId 삭제할 사용자 ID
     * @return 삭제된 행 수
     */
    int deleteProviderRequest(@Param("userId") String userId);
}