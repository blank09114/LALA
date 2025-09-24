package com.example.lala.Mapper;

import com.example.lala.DTO.Response.UserInterest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InterestMapper {
    List<UserInterest> selectUserInterested(String userId);
}
