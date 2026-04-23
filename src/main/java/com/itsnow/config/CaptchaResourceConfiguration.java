package com.itsnow.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import cloud.tianai.captcha.resource.impl.provider.ClassPathResourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static cloud.tianai.captcha.common.constant.CommonConstant.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH;

/**
 * @author itsnow
 * @date 2026/4/23
 */

@Configuration
public class CaptchaResourceConfiguration {

    /**
     * 配置验证码资源存储器
     * @return ResourceStore
     */
    @Bean
    public ResourceStore resourceStore() {
        // 使用简单的本地内存存储器，实际项目中可以使用数据库等存储
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();
        // 配置背景图
        // arg1: 验证码类型(SLIDER、ROTATE、CONCAT、WORD_IMAGE_CLICK)
        // arg2: Resource对象，包含: 资源类型(calsspath、file、url)、文件路径、tag标签

        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "META-INF/cut-image/resource/1.jpg"));
//        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "bgimages/2.png", "default"));
//        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "bgimages/3.png", "default"));
//        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "bgimages/4.png", "default"));


        return resourceStore;
    }

}

