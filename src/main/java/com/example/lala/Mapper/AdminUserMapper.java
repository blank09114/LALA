package com.example.lala.Mapper;

import com.example.lala.DTO.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 관리자 게시물 관련 데이터베이스 작업을 처리하는 MyBatis 매퍼 인터페이스
 * 
 * 관리자가 사용자별 게시물 정보를 조회하고 관리하기 위한 
 * 데이터베이스 쿼리 메서드들을 정의합니다.
 * 
 * MyBatis의 @Mapper 어노테이션을 통해 해당 인터페이스가 
 * 자동으로 구현체가 생성되어 스프링 컨테이너에 등록됩니다.
 * 
 * 매핑 파일: AdminUserMapper.xml
 */
@Mapper
public interface AdminUserMapper {

    /**
     * 특정 사용자가 작성한 모든 게시물 정보를 조회합니다.
     * 
     * 조회되는 정보:
     * - 작성자 닉네임
     * - 게시물 제목
     * - 작성 날짜
     * - 게시물 유형명
     * 
     * 이 메서드는 관리자가 특정 사용자의 활동 내역을 
     * 모니터링하거나 게시물 관리를 위해 사용됩니다.
     * 
     * @param user_id 조회할 사용자의 고유 식별자
     * @return 해당 사용자가 작성한 게시물 목록 (AdminPostResponse 리스트)
     */
//    List<AdminPost> postSelectOneUser(String user_id);
//



    List<UserDTO> findUsersByFilters(
            @Param("userTypes") List<String> userTypes,
            @Param("activeTypes") List<String> activeTypes,
            @Param("nickname") String nickname,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    /*

    Map 리스트로 작성한 이유
    userTypes : List
	•	activeTypes : List
	•	nickname : String
	•	startDate, endDate : LocalDate
	•	pageStart, pageEnd : Integer

	이러한 필터를 받아와야 하기 때문에
	또 파라미터가 너무 많아지기 때문에 Map<string, object> 로 작성하여 편리하게 불러오기 위해
	@Param으로 불러오게 된다면 순서를 정확히 하거나 모든 파라미터에 @Param을 붙여서 작성해야 하기 때문에 번거로워 짐.
     */
    List<UserDTO> findUsersByFilters(Map<String, Object> filters);

    // 전체 회원 수
    int countUsersByFilters(Map<String, Object> filters);

}