package com.example.lala;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.lala.Mapper")
@SpringBootApplication
public class LalaApplication {
//ㅇaa
	public static void main(String[] args) {
		SpringApplication.run(LalaApplication.class, args);
	}

}
