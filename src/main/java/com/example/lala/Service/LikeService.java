package com.example.lala.Service;

import com.example.lala.Mapper.LikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    public void unlikeLecture(String userId, String lectureId) {
        likeMapper.deleteLectureLike(userId, lectureId);
    }

    public void unlikeProvider(String userId, String providerId) {
        likeMapper.deleteProviderLike(userId, providerId);
    }
}
