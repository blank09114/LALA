package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Response.LikeLecture;
import com.example.lala.Mapper.LectureMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LectureService extends BaseService {
    private final LectureMapper mapper;

    public List<LikeLecture> selectLikeLecture(boolean free) {
        return selectLikeLecture(getCurrentUserId(), free);
    }

    public List<LikeLecture> selectLikeLecture(String id, boolean free) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", id);
        params.put("free", free);
        return mapper.selectLikeLecture(params);
    }

    // 페이지네이션을 지원하는 새로운 메소드
    public List<LikeLecture> selectLikeLecture(boolean free, int page, int size) {
        return selectLikeLecture(getCurrentUserId(), free, page, size);
    }

    public List<LikeLecture> selectLikeLecture(String userId, boolean free, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("free", free);
        params.put("offset", (page - 1) * size);
        params.put("limit", size);
        return mapper.selectLikeLecture(params);
    }
}