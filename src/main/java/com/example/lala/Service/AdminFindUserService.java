package com.example.lala.Service;

import com.example.lala.DTO.UserDTO;
import com.example.lala.Mapper.AdminUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 게시물 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 관리자가 사용자별 게시물 정보를 조회하는 기능을 제공합니다.
 */
@Service
@AllArgsConstructor
public class AdminFindUserService {

    /**
     * MyBatis 매퍼를 통해 데이터베이스와 상호작용하는 컴포넌트
     */
    private final AdminUserMapper adminUserMapper;

    /**
     * 특정 사용자가 작성한 모든 게시물을 조회합니다.
     * 
     * @param user_id 조회할 사용자의 고유 식별자
     * @return 사용자가 작성한 게시물 목록
     */
//    public List<AdminPost> selectPostUserOne(String user_id) {
//        return adminUserMapper.postSelectOneUser(user_id);
//    }

//
//    public List<UserDTO> findUserFilters(List<String> userTypes, List<String> activeTypes, String nickname, LocalDate startDate, LocalDate endDate){
//        return adminUserMapper.findUsersByFilters(userTypes, activeTypes, nickname, startDate, endDate);
//    }

    /*

    Map을 사용하지 않는 이유
    어떤 키가 들어오는지 IDE가 알 수 없음.
    타입을 추론할 수 없,
    런타임 잘못된 키나 타입으로 NPE가 날 수 있음.
     */
    public Map<String, Object> getUsersWithPaging(List<String> userTypes, List<String> activeTypes,
                                                   String nickname, LocalDate startDate, LocalDate endDate,
                                                   int page, int size){

        int offset = (page - 1) * size; // 시작페이지

        Map<String, Object> params = new HashMap<>();
        params.put("userTypes", userTypes);
        params.put("activeTypes", activeTypes);
        params.put("nickname", nickname);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("pageStart", offset);
        params.put("pageSize", size);

        // 위에 작성한 map 리스트가 들어옴
        List<UserDTO> users = adminUserMapper.findUsersByFilters(params);

        // 전체 회원수 count
        int total = adminUserMapper.countUsersByFilters(params);

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return result;
    }

}