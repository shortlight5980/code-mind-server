package com.itsnow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@Data
@Component
@ConfigurationProperties(prefix = "rsa")
public class RSAProperties {
    private String publicKey;
    private String privateKey;
}
