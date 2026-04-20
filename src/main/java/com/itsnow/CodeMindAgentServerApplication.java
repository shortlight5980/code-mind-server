package com.itsnow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itsnow.mapper")
public class CodeMindAgentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMindAgentServerApplication.class, args);
    }

}
