package com.itsnow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static com.itsnow.constant.RedisConstants.LOGIN_USER_KEY;
import static com.itsnow.constant.RedisConstants.LOGIN_USER_TTL;

/**
 * @author itsnow
 * @date 2026/4/22
 */
@Slf4j
@Component
public class LonginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        log.info("正在核实用户信息，token：{}", token);

        Object idObj = stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "id");
        if (idObj == null) {
            log.info("用户信息无效，驳回请求");
            response.setStatus(401);
            return false;
        }

        Long id = Long.valueOf(idObj.toString());

        // 刷新token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);

        log.info("核实成功，放行");
        return true;
    }
}
