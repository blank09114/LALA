package com.example.lala.Controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordPrintConfig {

    //@Bean
    public CommandLineRunner printEncodedPassword() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "1234";
            String encodedPassword = encoder.encode(rawPassword);
            System.out.println("=======================================");
            System.out.println("👉 인코딩된 비밀번호 (1234): " + encodedPassword);
            System.out.println("=======================================");
        };
    }
}