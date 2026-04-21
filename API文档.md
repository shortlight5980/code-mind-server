# API 文档

### 基础信息
- **基础路径**: `/api` (根据Spring Boot配置，可能需要调整)
- **数据格式**: JSON
- **字符编码**: UTF-8

---

### 通用响应格式

接口返回统一的 `Result` 格式：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应码，1 代表成功，0 代表失败 |
| msg | String | 响应信息描述 |
| data | Object | 返回的数据 |

---

## 接口列表

### 1. 健康检查

**接口描述**: 检查后端服务及Python AI端服务是否正常运行。

| 属性 | 值 |
|------|-----|
| **URL** | `/health` |
| **方法** | `GET` |
| **Content-Type** | `application/json` |

**请求参数**: 无

**请求示例**:
```
GET /health
```

**响应示例**:
```json
{
  "status": "ok"
}
```

---

### 2. 非流式聊天

**接口描述**: 发送问题给AI并一次性获取完整回复。

| 属性 | 值 |
|------|-----|
| **URL** | `/chat` |
| **方法** | `POST` |
| **Content-Type** | `application/json` |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| question | String | 是 | 用户问题 |
| sessionId | Long | 是 | 会话id   |

**请求示例**:
```json
{
  "question": "你好，请介绍一下自己",
  "sessionId": 1
}
```

**响应示例**:
```json
{
  "type": "ai",
  "content": "你好！我是AI助手，很高兴为你服务。"
}
```

---

### 3. 流式聊天

**接口描述**: 发送问题给AI并以SSE（Server-Sent Events）流式方式获取回复。

| 属性 | 值 |
|------|-----|
| **URL** | `/chat/stream` |
| **方法** | `POST` |
| **Content-Type** | `application/json` |
| **Accept** | `text/event-stream` |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| question | String | 是 | 用户问题 |
| sessionId | Long   | 是   | 会话id   |

**请求示例**:
```json
{
  "question": "你好，请介绍一下自己",
  "sessionId": 1
}
```

**响应格式**: SSE流式数据

每个数据块格式:
```
data: {
  "type": "ai",
  "content": "你好！"
}
```

**说明**:
- 响应以 `data: ` 前缀开头
- 流式传输结束时会收到 `[DONE]` 标记（已过滤）
- 消息会自动保存到数据库

---

### 4. 获取用户会话列表

**接口描述**: 根据用户ID获取该用户的所有会话历史，按最后活动时间倒序排列。

| 属性 | 值 |
|------|-----|
| **URL** | `/session` |
| **方法** | `GET` |
| **Content-Type** | `application/x-www-form-urlencoded` |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**请求示例**:
```
GET /session?id=1
```

**响应数据 (data)**:

返回 `SessionsVO` 数组：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 会话主键ID |
| title | String | 会话标题 |
| updatedTime | Date | 最后活动时间 |

**响应示例**:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "title": "默认会话",
      "updatedTime": "2026-04-21T10:30:00"
    }
  ]
}
```

---

### 5. 获取会话历史消息

**接口描述**: 获取指定会话的所有历史消息，按创建时间升序排列。

| 属性 | 值 |
|------|-----|
| **URL** | `/chat/history` |
| **方法** | `GET` |
| **Content-Type** | `application/x-www-form-urlencoded` |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**请求示例**:
```
GET /chat/history?sessionId=1
```

**响应数据 (data)**:

返回 `MessagesVO` 数组：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息主键ID |
| sessionId | Long | 所属会话ID |
| role | Integer | 角色: 0-SYSTEM, 1-USER, 2-ASSISTANT, 3-TOOL |
| content | String | 消息内容（已格式化处理） |
| createdAt | Date | 创建时间 |

**角色枚举说明**:

| 值 | 说明 |
|----|------|
| 0 | SYSTEM - 系统消息 |
| 1 | USER - 用户消息 |
| 2 | ASSISTANT - 助手回复 |
| 3 | TOOL - 工具调用消息 |

**响应示例**:

```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "sessionId": 1,
      "role": 1,
      "content": "你好",
      "createdAt": "2026-04-21T10:00:00"
    },
    {
      "id": 2,
      "sessionId": 1,
      "role": 2,
      "content": "你好！有什么可以帮助你的吗？",
      "createdAt": "2026-04-21T10:00:01"
    }
  ]
}
```

---

### 6. 新增会话

#### 接口描述

创建一个新的会话。

#### 接口信息

| 属性         | 值                 |
| ------------ | ------------------ |
| URL          | `/session`         |
| 方法         | POST               |
| Content-Type | `application/json` |

#### 请求参数

无

#### 请求示例

```http
POST /session
```

#### 响应示例

```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

#### 说明

- 新建会话默认使用用户 ID 为 1，仓库 ID 为 1
- 会话标题默认为 "新会话"
- 会话状态为 0（ACTIVE）



## 数据模型

### SessionsVO (会话视图对象)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键id |
| title | String | 会话标题 |
| updatedTime | Date | 最后活动时间 |

### MessagesVO (消息视图对象)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键id |
| sessionId | Long | 所属会话ID |
| role | Integer | 角色 (0-SYSTEM / 1-USER / 2-ASSISTANT / 3-TOOL) |
| content | String | 消息内容 |
| createdAt | Date | 创建时间 |

---

## 错误响应示例

**失败响应**:
```json
{
  "code": 0,
  "msg": "错误描述信息",
  "data": null
}
```
