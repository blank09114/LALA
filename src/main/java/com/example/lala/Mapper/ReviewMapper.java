package com.example.lala.Mapper;

import com.example.lala.DTO.Course.RegReview;
import com.example.lala.DTO.Response.MyReview;
import com.example.lala.DTO.Response.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper {
    List<Review> selectMyReview(String userId);
    int updateReview(Map<String, Object> params);
    int insertReview(Map<String, Object> params);
    MyReview checkReview(Map<String, Object> params);

    // 강의 상세에 들어가는 리뷰 등록
    void regReview(RegReview review);

    //강사 리뷰 조회
    List<com.example.lala.DTO.Course.Review> ipReviews(String userId);

    // 강의 상세에 들어가는 리뷰
    List<com.example.lala.DTO.Course.Review> lectureReviews(String lectureId);// 강의 상세에 들어가는 리뷰

    // 리뷰 점수순으로 조회
    List<com.example.lala.DTO.Course.Review> lectureReviewsOrderByRating(String lectureId);

    // 리뷰 점수순으로 조회
    List<com.example.lala.DTO.Course.Review> lectureReviewsOrderByRatingReverse(String lectureId);

    // 리뷰 최신순으로 조회
    List<com.example.lala.DTO.Course.Review> lectureReviewsRecent(String lectureId);


    // 유틸리티
    String findWriterByReviewId(String reviewId);
}
