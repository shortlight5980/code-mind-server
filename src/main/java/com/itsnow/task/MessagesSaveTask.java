package com.itsnow.task;

import com.itsnow.service.SessionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private final ExecutorService executorService = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "message-save-")
    );

    /**
     * 每10分钟将redis中的数据保存到数据库中
     */
    @Scheduled(fixedDelay = 1000 * 60 * 10)
    public void saveMessages() {
        // 定时持久化任务逻辑
        try {
            // 查询set集合session:index中的所有元素
            Set<String> keys = stringRedisTemplate.opsForSet().members(MESSAGES_HISTORY_INDEX_KEY);

            int count = (keys == null) ? 0 : keys.size();
            log.info("===========================================================================================================");
            log.info("定时持久化任务开始执行，共有{}个会话需要处理", count);
            log.info("===========================================================================================================");

            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (String key : keys) {
                try {
                    String[] split = key.split(":");
                    Long sessionId = Long.valueOf(split[split.length - 1].strip());
                    executorService.submit(() -> {
                        try {
                            sessionsService.endSessionInternal(sessionId);
                        } catch (Exception e) {
                            log.error("持久化会话失败，sessionId: {}", sessionId, e);
                        }
                    });
                } catch (Exception e) {
                    log.error("处理会话key失败，key: {}", key, e);
                }
            }
        } catch (Exception e) {
            log.error("定时持久化任务执行异常", e);
        }
    }
}
