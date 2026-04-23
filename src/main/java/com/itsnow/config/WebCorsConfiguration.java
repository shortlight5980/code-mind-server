package com.itsnow.config;

import com.itsnow.interceptor.LonginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author itsnow
 * @date 2026/4/20
 */
@Configuration
public class WebCorsConfiguration implements WebMvcConfigurer {

    @Autowired
    private LonginInterceptor longinInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(longinInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/genCaptcha",
                        "/check",
                        "/user/regist",
                        "/user/code",
                        "/user/emailCode",
                        "/rsa/**",
                        "/health"
                );
    }
}
