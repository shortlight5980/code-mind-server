package com.itsnow;

import com.itsnow.utils.RSAUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@SpringBootTest
public class RSAUtilsTest {
    @Test
    public void testRSAGenerate() throws Exception {
        KeyPair keyPair = RSAUtils.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        System.out.println("公钥：" + RSAUtils.publicKeyToBase64(publicKey));
        System.out.println("私钥：" + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
    }
}
