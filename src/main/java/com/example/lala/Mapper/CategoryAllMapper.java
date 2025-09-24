package com.example.lala.Mapper;

import com.example.lala.DTO.Request.CategorySelection;
import com.example.lala.DTO.Response.Category;
import com.example.lala.DTO.Response.CategoryDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 카테고리 관련 데이터베이스 작업을 처리하는 MyBatis 매퍼 인터페이스
 * <p>
 * 시스템의 카테고리 정보 조회, 사용자별 관심 카테고리 관리 등의
 * 데이터베이스 작업을 수행하는 메서드들을 정의합니다.
 * <p>
 * 주요 기능:
 * - 전체 카테고리 목록 조회
 * - 사용자별 선택한 카테고리 조회
 * - 사용자 관심 카테고리 등록
 * <p>
 * 매핑 파일: CategoryAllMapper.xml (예상)
 */
@Mapper
public interface CategoryAllMapper {

    /**
     * 시스템에 등록된 모든 카테고리 정보를 조회합니다.
     * <p>
     * 사용자가 관심 카테고리를 선택하거나 카테고리별 콘텐츠를
     * 탐색할 때 사용할 전체 카테고리 목록을 제공합니다.
     *
     * @return 전체 카테고리 목록 (CategoryResponse 리스트)
     */
    List<Category> categoryAll();

    /**
     * 특정 사용자가 선택한 관심 카테고리 목록을 조회합니다.
     * <p>
     * 사용자별로 개인화된 콘텐츠 추천이나 맞춤형 서비스 제공을
     * 위해 해당 사용자의 관심 카테고리 정보를 조회합니다.
     *
     * @param user_id 조회할 사용자의 고유 식별자
     * @return 사용자가 선택한 카테고리 상세 정보 목록 (CategoryDetailResponse 리스트)
     */
    List<CategoryDetail> UserSelectCategory(String userId);

    /**
     * 사용자의 관심 카테고리 선택 정보를 데이터베이스에 저장합니다.
     * <p>
     * 회원가입 시 또는 설정 변경 시 사용자가 선택한 관심 카테고리를
     * 시스템에 등록하여 개인화된 서비스 제공의 기반 데이터로 활용합니다.
     *
     * @param categorySelectDTO 사용자가 선택한 카테고리 정보를 담은 요청 객체
     */
    void insertCategory(CategorySelection categorySelectDTO);

    /**
     * 특정 사용자의 모든 관심 카테고리를 삭제합니다.
     * <p>
     * 사용자의 관심사 설정을 변경할 때 기존 데이터를 삭제하기 위해 사용됩니다.
     *
     * @param userId 삭제할 사용자의 고유 식별자
     */
    void deleteUserCategories(String userId);

}