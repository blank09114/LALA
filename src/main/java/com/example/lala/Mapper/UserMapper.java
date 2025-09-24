package com.example.lala.Mapper;

import com.example.lala.DTO.Response.InformationProvider;
import com.example.lala.DTO.Response.UserProfile;
import com.example.lala.DTO.UserCommon;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;
import java.util.Optional;

@Mapper
public interface UserMapper {
    UserProfile selectById(String userId);

    int updatePassword(Map<String, Object> params);

    int updateNickname(Map<String, Object> params);

    int updateProfileImage(Map<String, Object> params);

    UserCommon selectUserById(String userId);

    String getCurrentPassword(String userId);

    int updateUserType(String userId, String userType);


}