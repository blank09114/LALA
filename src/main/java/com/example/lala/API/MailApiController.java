package com.example.lala.API;

import com.example.lala.DTO.Auth.EmailRequest;
import com.example.lala.DTO.Auth.EmailVerificationRequest;
import com.example.lala.Service.MailService;
import com.example.lala.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 이메일 인증 및 비밀번호 관련 REST API 컨트롤러
 * 
 * 주요 기능:
 * - 이메일 인증 코드 발송
 * - 이메일 인증 코드 검증
 * - 이메일 존재 여부 확인
 * - 임시 비밀번호 발급
 */
@RestController
@RequiredArgsConstructor
@EnableAsync
public class MailApiController {

    private final MailService mailService;
    private final UserService userService;

    /**
     * 이메일 인증 코드 발송
     * 
     * @param emailRequest 이메일 주소를 포함한 요청 데이터
     * @return CompletableFuture<String> 생성된 인증 코드를 문자열로 반환
     */
    @PostMapping("/auth/mail")
    public CompletableFuture<String> mailsend(@RequestBody EmailRequest emailRequest){
        return mailService.sendMail(emailRequest.getMail())
                .thenApply(number -> String.valueOf(number));
    }

    /**
     * 이메일 인증 코드 검증
     * 
     * @param verificationRequest 이메일 주소와 인증 코드를 포함한 요청 데이터
     * @return String 검증 결과 메시지 ("Verified" 또는 "Verification failed")
     */
    @PostMapping("/auth/verify-code")
    public String verifyCode(@RequestBody EmailVerificationRequest verificationRequest){
        boolean isVerified = mailService.verifyCode(
            verificationRequest.getEmailAddress(), 
            verificationRequest.getVerificationCode()
        );
        return isVerified ? "Verified" : "Verification failed";
    }

    /**
     * 이메일 존재 여부 확인 (회원가입 시 중복 체크용)
     * 
     * @param emailRequest 확인할 이메일 주소를 포함한 요청 데이터
     * @return boolean 이메일이 이미 등록되어 있으면 true, 없으면 false
     */
    @PostMapping("/auth/check-email")
    public boolean checkEmail(@RequestBody EmailRequest emailRequest) {
        return userService.existsByEmail(emailRequest.getMail());
    }

    /**
     * 사용자 ID(이메일) 존재 여부 확인
     * 
     * @param request 이메일 주소를 포함한 Map 형태의 요청 데이터
     * @return boolean 해당 이메일로 가입된 사용자가 있으면 true, 없으면 false
     */
    @PostMapping("/auth/check-userid")
    public boolean checkUserId(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return userService.existsByEmail(email);
    }

    /**
     * 임시 비밀번호 발급 및 이메일 전송
     * 
     * 비밀번호를 찾기 위한 기능으로, 등록된 이메일로 임시 비밀번호를 발송합니다.
     * 임시 비밀번호는 자동으로 생성되며, 사용자의 기존 비밀번호를 대체합니다.
     * 
     * @param emailRequest 임시 비밀번호를 받을 이메일 주소를 포함한 요청 데이터
     * @return ResponseEntity<String> 
     *         - 성공 시: HTTP 200과 발송 완료 메시지
     *         - 실패 시: HTTP 400과 사용자 없음 메시지
     */
    @PostMapping("/auth/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody EmailRequest emailRequest){
        String email = emailRequest.getMail();

        if(userService.existsByEmail(email)){
            String tempPassword = mailService.createTemporaryPassword(email);
            mailService.sendTemporaryPasswordMail(email, tempPassword);
            return ResponseEntity.ok("임시 비밀번호가 발송되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("해당 이메일로 가입된 사용자가 없습니다.");
        }
    }
}