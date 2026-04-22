package com.itsnow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@MapperScan("com.itsnow.mapper")
public class CodeMindAgentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMindAgentServerApplication.class, args);
    }

}
