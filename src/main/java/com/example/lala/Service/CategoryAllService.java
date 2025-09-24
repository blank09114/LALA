package com.example.lala.Service;

import com.example.lala.DTO.Request.CategorySelection;
import com.example.lala.DTO.Response.Category;
import com.example.lala.DTO.Response.CategoryDetail;
import com.example.lala.Mapper.CategoryAllMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 카테고리 및 사용자 관심사 관리를 위한 서비스 클래스
 * MyBatis 매퍼를 통해 카테고리 정보 조회와 사용자별 관심사 등록/조회 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 전체 카테고리 목록 조회
 * - 사용자별 선택된 관심사 카테고리 조회
 * - 사용자 관심사 카테고리 등록
 */
@Service
@AllArgsConstructor
public class CategoryAllService {

    /**
     * 카테고리 관련 데이터베이스 작업을 처리하는 MyBatis 매퍼
     * 카테고리 목록 조회, 사용자 관심사 조회 및 등록 기능을 제공합니다.
     */
    private final CategoryAllMapper categoryAll;

    /**
     * 시스템에서 제공하는 전체 카테고리 목록을 조회합니다.
     * 사용자가 관심사를 선택할 때 표시될 카테고리 옵션들을 반환합니다.
     * 
     * @return 전체 카테고리 정보를 담은 CategoryResponse 리스트
     */
    public List<Category> categoryAll(){
        return categoryAll.categoryAll();
    }

    /**
     * 특정 사용자가 선택한 관심사 카테고리 목록을 조회합니다.
     * 사용자 개인화 페이지나 프로필에서 현재 설정된 관심사를 표시할 때 사용됩니다.
     * 
     * @param user_id 조회할 사용자의 고유 식별자
     * @return 해당 사용자가 선택한 관심사 카테고리의 상세 정보를 담은 CategoryDetailResponse 리스트
     */
    public List<CategoryDetail> userCategory(String userId){
        return categoryAll.UserSelectCategory(userId);
    }

    /**
     * 사용자가 선택한 관심사 카테고리들을 데이터베이스에 등록합니다.
     * 회원가입 시 관심사 설정이나 기존 사용자의 관심사 변경 시 사용됩니다.
     * 
     * 동작 과정:
     * 1. 기존 사용자의 관심사 데이터 삭제 (있는 경우)
     * 2. 새로운 관심사 목록을 일괄 등록
     * 
     * @param dto 사용자 ID와 선택된 관심사 카테고리 ID 목록을 포함하는 요청 객체
     */
    public void insertUserCategory(CategorySelection dto) {
        // 기존 사용자의 관심사 데이터 삭제
        categoryAll.deleteUserCategories(dto.getUserId());
        
        // 새로운 관심사 목록이 있는 경우에만 추가
        if (dto.getInterestedId() != null && !dto.getInterestedId().isEmpty()) {
            categoryAll.insertCategory(dto);
        }
    }
}