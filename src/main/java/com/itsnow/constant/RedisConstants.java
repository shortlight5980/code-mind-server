package com.itsnow.constant;

/**
 * @author itsnow
 * @date 2026/4/22
 */
public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final String MESSAGES_HISTORY_KEY = "session:";
    public static final Long MESSAGES_HISTORY_TTL = 60L;

    public static final String MESSAGES_HISTORY_INDEX_KEY = "session:index";

    public static final Long CACHE_NULL_TTL = 2L;

    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String USER_SIGN_KEY = "sign:";
}
