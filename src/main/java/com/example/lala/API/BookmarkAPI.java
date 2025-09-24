package com.example.lala.API;

import com.example.lala.Mapper.ProviderBookmarkMapper;
import com.example.lala.Service.ProviderBookmarkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/bookmark")
@AllArgsConstructor
public class BookmarkAPI {

    private final ProviderBookmarkService providerBookmarkService;


    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleProviderBookmark(@RequestBody Map<String, String> request) {
        String providerId = request.get("providerId");


        boolean liked = providerBookmarkService.toggleBookmark(providerId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked); // true: 찜됨, false: 찜 해제됨

        return ResponseEntity.ok(result);
    }

}
