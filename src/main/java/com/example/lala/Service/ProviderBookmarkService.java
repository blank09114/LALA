package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.Mapper.ProviderBookmarkMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProviderBookmarkService extends BaseService {

    private final ProviderBookmarkMapper providerBookmarkMapper;

    public void deleteProviderBookmark(String providerId){

        String userId = getCurrentUserId();

        System.out.println("여기서 userId = ?" + userId);



        providerBookmarkMapper.removeProviderBookmark(userId, providerId);
    }

    public boolean toggleBookmark(String providerId) {

        String userId = getCurrentUserId();

        boolean exists = providerBookmarkMapper.checkProviderBookmark(userId, providerId);
        if (exists) {
            providerBookmarkMapper.removeProviderBookmark(userId, providerId);
            return false;
        } else {
            providerBookmarkMapper.addProviderBookmark(userId, providerId);
            return true;
        }
    }



}
