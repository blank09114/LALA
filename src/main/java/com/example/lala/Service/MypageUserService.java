package com.example.lala.Service;

import com.example.lala.Config.BaseService;
import com.example.lala.Mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageUserService extends BaseService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void updateNickname(String nickname) {
        updateNickname(getCurrentUserId(), nickname);
    }

    public void updateNickname(String userId, String nickname) {
        Map<String, Object> params = Map.of("userId", userId, "nickname", nickname);
        try {
            int rows = userMapper.updateNickname(params);
            logOperation("닉네임 업데이트", userId, rows > 0);

            if (rows == 0) {
                throw new RuntimeException("닉네임 업데이트에 실패했습니다.");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
    }

    /**
     * 현재 비밀번호를 확인합니다.
     *
     * @param currentPassword 확인할 현재 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean verifyCurrentPassword(String currentPassword) {
        try {
            String userId = getCurrentUserId();
            String storedPassword = userMapper.getCurrentPassword(userId);

            boolean result = passwordEncoder.matches(currentPassword, storedPassword);
            System.out.println("result" + result);
            return result;
        } catch (Exception e) {
            logOperation("비밀번호 확인", getCurrentUserId(), false);
            throw new RuntimeException("비밀번호 확인 중 오류가 발생했습니다.", e);
        }
    }

    public void updatePassword(String password) {
        updatePassword(getCurrentUserId(), password);
    }

    public void updatePassword(String userId, String password) {
        // 비밀번호를 BCrypt로 암호화
        String encodedPassword = passwordEncoder.encode(password);

        Map<String, Object> params = Map.of("userId", userId, "password", encodedPassword);
        int rows = userMapper.updatePassword(params);
        logOperation("비밀번호 업데이트", userId, rows > 0);

        if (rows == 0) {
            throw new RuntimeException("비밀번호 업데이트에 실패했습니다.");
        }
    }

}