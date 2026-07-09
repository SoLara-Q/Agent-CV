package com.hrq.aiinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hrq.aiinterview.mapper")
public class AiInterviewPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiInterviewPlatformApplication.class, args);
    }



}
