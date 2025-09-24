package com.example.lala.Service;

import com.example.lala.Entity.User;
import com.example.lala.JPARepository.JPAUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * 이메일 서비스 구현 클래스
 * 사용자 회원가입 시 이메일 인증과 비밀번호 재설정을 위한 이메일 발송 기능을 제공합니다.
 * SMTP를 통해 HTML 형식의 이메일을 발송하고, 인증코드와 임시 비밀번호 검증을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JPAUserRepository JPAUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    
    /**
     * 시스템에서 사용하는 발신자 이메일 주소
     */
    private static final String senderEmail = "ehddn5476@gmail.com";
    
    /**
     * 이메일별 인증코드를 임시 저장하는 메모리 저장소
     * 실제 운영환경에서는 Redis 등의 외부 저장소 사용을 권장합니다.
     */
    private static final Map<String, Integer> verificationCodes = new HashMap<>();

    /**
     * 6자리 랜덤 인증번호를 생성하고 메모리에 저장합니다.
     * 
     * @param mail 인증번호를 생성할 이메일 주소
     */
    private static void createNumber(String mail) {
        int number = new Random().nextInt(900000) + 100000;
        verificationCodes.put(mail, number);
    }

    /**
     * 이메일 인증을 위한 HTML 메일 메시지를 생성합니다.
     * 
     * @param mail 인증 메일을 받을 이메일 주소
     * @return 생성된 MIME 메시지 객체
     */
    @Override
    public MimeMessage createMail(String mail) {
        createNumber(mail);
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(mail);
            helper.setSubject("이메일 인증번호");
            
            // HTML 형식의 이메일 본문 생성
            String body = "<h2>LALA에 오신걸 환영합니다!</h2>" +
                         "<h3>아래의 인증번호를 입력하세요.</h3>" +
                         "<h1>" + verificationCodes.get(mail) + "</h1>" +
                         "<h3>감사합니다.</h3>";
            helper.setText(body, true);

        } catch (MessagingException e) {
            log.error("이메일 생성 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 사용자가 입력한 인증 코드를 검증합니다.
     * 
     * @param mail 인증을 요청한 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 시 true, 실패 시 false
     */
    @Override
    public boolean verifyCode(String mail, int code) {
        Integer storedCode = verificationCodes.get(mail);
        return storedCode != null && storedCode == code;
    }

    /**
     * 이메일 인증 메일을 비동기적으로 발송합니다.
     * 메일 발송으로 인한 응답 지연을 방지하기 위해 비동기 처리를 적용했습니다.
     * 
     * @param mail 인증 메일을 받을 이메일 주소
     * @return 생성된 인증 코드를 담은 CompletableFuture 객체
     */
    @Async
    @Override
    public CompletableFuture<Integer> sendMail(String mail) {
        MimeMessage message = createMail(mail);
        javaMailSender.send(message);
        log.info("인증 메일 발송 완료: {}", mail);
        return CompletableFuture.completedFuture(verificationCodes.get(mail));
    }

    /**
     * 8자리 영문자와 숫자로 구성된 랜덤 임시 비밀번호를 생성합니다.
     * 
     * @return 생성된 임시 비밀번호 문자열
     */
    private static String generateRandomPassword() {
        int length = 8;
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    /**
     * 사용자의 임시 비밀번호를 생성하고 데이터베이스에 저장합니다.
     * 생성된 임시 비밀번호는 암호화되어 저장되며, 평문으로 반환됩니다.
     * 
     * @param email 임시 비밀번호를 발급받을 사용자의 이메일 주소
     * @return 생성된 임시 비밀번호 (평문)
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Override
    public String createTemporaryPassword(String email) {
        String tempPassword = generateRandomPassword();
        User user = JPAUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 임시 비밀번호를 암호화하여 데이터베이스에 저장
        user.setUserPassword(passwordEncoder.encode(tempPassword));
        JPAUserRepository.save(user);
        
        log.info("임시 비밀번호 생성 완료: {}", email);
        return tempPassword;
    }

    /**
     * 사용자가 입력한 임시 비밀번호가 올바른지 검증합니다.
     * 
     * @param email 비밀번호 재설정을 요청한 이메일 주소
     * @param tempPassword 사용자가 입력한 임시 비밀번호
     * @return 검증 성공 시 true, 실패 시 false
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Override
    public boolean verifyTemporaryPassword(String email, String tempPassword) {
        User user = JPAUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 입력된 평문 비밀번호와 암호화된 비밀번호를 비교
        return passwordEncoder.matches(tempPassword, user.getUserPassword());
    }

    /**
     * 임시 비밀번호를 포함한 HTML 이메일을 발송합니다.
     * 
     * @param email 임시 비밀번호를 받을 이메일 주소
     * @param tempPassword 발송할 임시 비밀번호
     * @throws RuntimeException 이메일 발송 실패 시
     */
    @Override
    public void sendTemporaryPasswordMail(String email, String tempPassword) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("LALA 임시 비밀번호");
            
            // HTML 형식의 임시 비밀번호 메일 본문 생성
            String body = "<h2>LALA에 오신걸 환영합니다!</h2>" +
                         "<p>아래의 임시 비밀번호를 사용하세요.</p>" +
                         "<h1>" + tempPassword + "</h1>" +
                         "<h3>반드시 비밀번호를 재설정하세요.</h3>";
            helper.setText(body, true);
            
            javaMailSender.send(message);
            log.info("임시 비밀번호 메일 발송 완료: {}", email);
            
        } catch (MessagingException e) {
            log.error("임시 비밀번호 메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("임시 비밀번호 전송 오류", e);
        }
    }
}