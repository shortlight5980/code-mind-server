package com.itsnow.task;

import com.itsnow.service.SessionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.itsnow.constant.RedisConstants.MESSAGES_HISTORY_INDEX_KEY;

/**
 * @author itsnow
 * @date 2026/4/24
 */
@Component
@Slf4j
public class MessagesSaveTask {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SessionsService sessionsService;
    /**
     * 每10分钟将redis中的数据保存到数据库中
     */
    @Scheduled(fixedDelay = 1000 * 60 * 10)
    public void saveMessages() {
        // 定时持久化任务逻辑
        // 查询set集合session:index中的所有元素
        Set<String> keys = stringRedisTemplate.opsForSet().members(MESSAGES_HISTORY_INDEX_KEY);

        Integer count = 0;
        if (keys == null) {
            count = 0;
        } else {
            count = keys.size();
        }
        System.out.println("===========================================================================================================");
        log.info("定时持久化任务开始执行，共有{}个会话需要处理", count);
        System.out.println("===========================================================================================================");

        // 遍历，获取每个元素
        for (String key : keys) {
            String[] split = key.split(":");
            Long sessionId = Long.valueOf(split[split.length - 1].strip());

            // 持久化并设置过期时间
            sessionsService.endSessionInternal(sessionId);
        }
    }
}
