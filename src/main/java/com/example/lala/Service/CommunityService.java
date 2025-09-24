package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.DTO.Request.MyComm;
import com.example.lala.DTO.Request.PageRequest;
import com.example.lala.DTO.Response.MyCommunity;
import com.example.lala.DTO.Response.PageResponse;
import com.example.lala.Mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService extends BaseService {

    private final CommunityMapper mapper;

    public PageResponse<MyCommunity> selectMyComm(MyComm myComm) {
        return selectMyComm(getCurrentUserId(), myComm);
    }

    public PageResponse<MyCommunity> selectMyComm(String userId, MyComm filter) {
        PageRequest pageRequest = filter.getPageRequest();
        if (pageRequest == null) {
            pageRequest = new PageRequest(1, 10);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("postType", filter.getPostType());
        params.put("contentCheck", filter.getContentCheck());
        params.put("deleted", filter.getDeleted());
        params.put("pageSize", pageRequest.getSize());
        params.put("offset", pageRequest.getOffset());

        try {
            List<MyCommunity> content = mapper.selectMyComm(params);
            long totalElements = mapper.countMyComm(params);
            
            logOperation("내 커뮤니티 조회", userId, true);
            return new PageResponse<>(content, pageRequest.getPage(), pageRequest.getSize(), totalElements);
        } catch (Exception e) {
            logOperation("내 커뮤니티 조회", userId, false);
            throw new RuntimeException("커뮤니티 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }

    public PageResponse<MyCommunity> selectReportedComm(Integer[] postType, PageRequest pageRequest) {
        if (pageRequest == null) {
            pageRequest = new PageRequest(1, 10);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", getCurrentUserId());
        params.put("postType", postType);
        params.put("pageSize", pageRequest.getSize());
        params.put("offset", pageRequest.getOffset());

        try {
            List<MyCommunity> content = mapper.selectReportedComm(params);
            long totalElements = mapper.countReportedComm(params);
            
            return new PageResponse<>(content, pageRequest.getPage(), pageRequest.getSize(), totalElements);
        } catch (Exception e) {
            throw new RuntimeException("신고한 커뮤니티 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }
}