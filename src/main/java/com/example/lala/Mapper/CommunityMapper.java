package com.example.lala.Mapper;

import com.example.lala.DTO.Response.CommunityPost;
import com.example.lala.DTO.Response.MyCommunity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {
    
    List<CommunityPost> selectLastComm(String userId);
    
    List<MyCommunity> selectMyComm(Map<String, Object> params);
    
    long countMyComm(Map<String, Object> params);
    
    List<MyCommunity> selectReportedComm(Map<String, Object> params);
    
    long countReportedComm(Map<String, Object> params);
}