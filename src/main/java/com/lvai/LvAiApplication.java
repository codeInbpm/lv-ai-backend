package com.lvai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lvai.mapper")
public class LvAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LvAiApplication.class, args);
    }
}
