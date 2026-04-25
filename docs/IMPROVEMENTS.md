# 项目改进建议

本文档记录了代码审查中发现的待改进问题，按优先级排序。

## 已修复问题

✅ 问题1: MessagesSaveTask 定时任务异常处理
✅ 问题2: JSON 字符串拼接改为使用 Hutool JSON 库
✅ 问题3: 随机消息 ID 改为使用 tmpId 方案
✅ 问题6: 拦截器多次访问 Redis 改为使用 entries()
✅ 问题7: 删除注释代码
✅ 问题8: System.out.println 改为 log
✅ 问题9: LonginInterceptor → LoginInterceptor（已修复）
✅ 枚举: 添加 MessageRole 枚举

---

## 待修复问题

### 🔴 高优先级

#### 问题4: 缺少分布式锁
- **位置**: MessagesSaveTask
- **问题**: 如果部署多个实例，定时任务会同时执行，导致同一条消息被持久化多次
- **建议**: 使用 Redis 分布式锁
  - 方案1: 使用 SETNX + 过期时间
  - 方案2: 集成 Redisson 库
  - 建议实现: 获取锁后再执行任务，锁超时时间大于任务执行时间

#### 问题5: 定时任务单线程串行处理
- **位置**: MessagesSaveTask.saveMessages()
- **问题**: for 循环串行处理所有会话，会话量大时性能差
- **当前状态**: 已添加线程池并行处理
- **优化建议**: 
  - 考虑使用分片处理
  - 增加线程池监控
  - 考虑使用 Spring @Async

---

### 🟡 中优先级

#### 问题10: 硬编码常量
- **位置**: SessionsServiceImpl.DEFAULT_REPO_ID = 1
- **问题**: 魔法数字应该配置化
- **建议**: 
  - 在 application.yml 中配置
  - 使用 @ConfigurationProperties 绑定

- **位置**: role 魔法数字 (0/1/2/3)
- **当前状态**: 已创建 MessageRole 枚举，待集成使用
- **建议**: 
  - 在 Messages 和 MessagesVO 中使用枚举类型
  - 替换所有 switch case 中的数字

#### 问题11: 缺少参数校验
- **位置**: Controller 层
- **问题**: 没有对入参进行 @Valid 校验
- **风险**: 容易出现空指针、格式错误
- **建议**: 
  - 添加 spring-boot-starter-validation 依赖
  - 在 DTO/VO 中添加校验注解 (@NotNull, @NotBlank, @Min, @Max 等)
  - 在 Controller 的 @RequestBody 参数前添加 @Valid

#### 问题12: 事务边界不清晰
- **位置**: ChatServiceImpl.chatStream()
- **问题**: 有 @Transactional 注解，但实际操作的是 Redis
- **建议**: 
  - 明确事务范围
  - 考虑是否需要 @Transactional
  - 如需事务，考虑使用 Redis 事务或 ChainedTransactionManager

---

### 🟢 低优先级

#### 问题13: AI 服务调用没有降级/熔断
- **位置**: ChatServiceImpl.chat() 和 chatStream()
- **问题**: 如果 Python AI 服务挂了，整个聊天会失败
- **建议**: 
  - 集成 Resilience4j 或 Sentinel
  - 添加降级逻辑（返回友好提示）
  - 添加重试机制

#### 问题14: 没有对 Redis 操作做异常处理
- **问题**: Redis 故障时服务会直接报错
- **建议**: 
  - 添加降级逻辑（部分功能可用）
  - 使用 Spring Cache 的错误处理
  - 关键操作 try-catch 并记录日志

#### 问题15: 消息历史没有限制条数
- **问题**: 会话持续很久时，历史消息会非常大
- **建议**: 
  - 限制上下文窗口大小（如最近 20 条）
  - 或限制 Token 数量
  - 在 _getHistoryMessages 中实现截断逻辑

---

## 其他建议

### 代码质量
1. **字段注入**: 推荐使用构造器注入替代 @Autowired 字段注入
2. **日期格式**: 统一使用 java.time 包下的类（LocalDateTime 替代 Date）
3. **常量管理**: 将 Redis Key 前缀、TTL 等集中管理到 RedisConstants 中（已有）

### 可观测性
1. **日志完善**: 添加更多关键路径的日志
2. **指标监控**: 集成 Micrometer，添加业务指标
3. **链路追踪**: 考虑集成 Sleuth / SkyWalking

### 测试
1. **单元测试**: 为核心业务逻辑添加单元测试
2. **集成测试**: 添加 Redis + MySQL 集成测试
3. **性能测试**: 定时任务的性能压测

---

## 优先级总结

| 优先级 | 问题 | 说明 |
|--------|------|------|
| P0 | - | 已全部修复 |
| P1 | 4, 10, 11 | 分布式锁、配置化、参数校验 |
| P2 | 5, 12, 13, 14, 15 | 性能优化、熔断降级、安全处理 |

