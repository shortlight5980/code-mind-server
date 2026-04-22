package com.itsnow.controller;

import com.itsnow.config.RSAProperties;
import com.itsnow.domain.pojo.Result;
import com.itsnow.utils.RSAUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@RestController
@RequestMapping("/rsa")
public class RSAController {

    private final RSAProperties rsaProperties;

    public RSAController(RSAProperties rsaProperties) {
        this.rsaProperties = rsaProperties;
    }

    // 获取公钥给前端
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        String publicKeyBase64 = rsaProperties.getPublicKey();
        return Result.success(publicKeyBase64);
    }

    // 前端加密后传输的数据
    @PostMapping("/encrypted-data")
    public Result handleEncryptedData(@RequestBody Map<String, String> request) throws Exception {
        String encryptedData = request.get("data");
        String decryptedData = RSAUtils.decrypt(encryptedData, RSAUtils.base64ToPrivateKey(rsaProperties.getPrivateKey()));
        System.out.println("解密后的数据: " + decryptedData);
        // 处理业务逻辑...
        return Result.success();
    }
}
