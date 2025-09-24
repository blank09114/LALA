package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Response.AdminLecuterList;
import com.example.lala.Mapper.AdminLectureMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AdminFindLectureService extends BaseService {

    private AdminLectureMapper lectureMapper;

    public Map<String, Object> findUserLectureFilters(List<String> requestType,
                                                      String nickname,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      int page, int size){

        String userId = getCurrentUserId();

        int offset = (page - 1) * size; // 시작페이지

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("requestType", requestType);
        params.put("nickname", nickname);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("pageStart", offset);
        params.put("pageSize", size);


        List<AdminLecuterList> lectures = lectureMapper.findUserLectureFilters(params);

        int total = lectureMapper.countLecturesByFilters(params);

        Map<String, Object> result = new HashMap<>();
        result.put("lectures", lectures);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return result;
    }
}
