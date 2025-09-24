package com.example.lala.Mapper;


import com.example.lala.DTO.Response.AdminUserActivity;
import com.example.lala.DTO.Response.ReportDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AdminPostMapper {

    List<ReportDetail> findReportsByPostId(String postId);

    List<ReportDetail> findReportsByCommentId(String commentId);

    List<ReportDetail> findReportsByReviewId(String reviewId);

    List<AdminUserActivity> findUserActivities(
            @Param("userId") String userId,
            @Param("title") String title,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userType") Integer userType
    );

    List<AdminUserActivity> findReportedByMe(
            @Param("userId") String userId,
            @Param("title") String title,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    List<AdminUserActivity> findReportedToMe(
            @Param("userId") String userId,
            @Param("title") String title,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}


