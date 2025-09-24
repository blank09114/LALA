package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Response.AdminCommunityList;
import com.example.lala.Mapper.AdminCommunityMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AdminFindCommunityService extends BaseService {

    private final AdminCommunityMapper adminCommunityMapper;



    public Map<String , Object> findUserCommunityFilters(List<String> postTypes,
                                                         List<String> userTypes,
                                                         List<String> reports,
                                                         String nickname,
                                                         LocalDate startDate,
                                                         LocalDate endDate,
                                                         int page, int size){


        String userId = getCurrentUserId();
        int offset = (page - 1) * size; // 시작페이지

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("postTypes", postTypes);
        params.put("userTypes", userTypes);
        params.put("reports", reports);
        params.put("nickname", nickname);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("pageStart", offset);
        params.put("pageSize", size);

        List<AdminCommunityList> communitys = adminCommunityMapper.findUserCommunityFilters(params);

        int total = adminCommunityMapper.countCommunitysByFilters(params);

        Map<String, Object> result = new HashMap<>();
        result.put("communitys", communitys);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return result;
    }
}
