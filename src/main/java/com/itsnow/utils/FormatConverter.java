package com.itsnow.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author itsnow
 * @date 2026/4/21
 */
public class FormatConverter {

    /**
     * 处理 Agent 流式输出的 chunk，进行消息格式化
     *
     * @param chunk 原始 chunk 字符串
     * @return 格式化后的 JSON 字符串，跳过的消息返回 null
     */
    public static String processChunk(String chunk) {
        JSONObject json = JSONUtil.parseObj(chunk);
        String type = json.getStr("type");

        JSONObject message = new JSONObject();
        JSONObject data = json.getJSONObject("data");

        if ("human".equals(type)) {
            message.set("type", "human");
            message.set("content", data.getStr("content"));
            return JSONUtil.toJsonStr(message);
        }

        if ("ai".equals(type)) {
            message.set("type", "ai");
            message.set("content", data.getStr("content"));
            message.set("id", data.getStr("id"));

            // 提取并简化 tool_calls
            List<JSONObject> rawToolCalls = data.getBeanList("tool_calls", JSONObject.class);
            List<JSONObject> simplifiedToolCalls = new ArrayList<>();
            if (rawToolCalls != null) {
                for (JSONObject tc : rawToolCalls) {
                    JSONObject simplified = new JSONObject();
                    simplified.set("id", tc.getStr("id"));
                    simplified.set("name", tc.getStr("name"));

                    Object args = tc.get("args");
                    if (args instanceof cn.hutool.json.JSONNull || args == null) {
                        simplified.put("args", null);
                    } else {
                        simplified.put("args", args);
                    }

                    simplifiedToolCalls.add(simplified);
                }
            }
            message.set("tool_calls", simplifiedToolCalls);
        } else if ("tool".equals(type)) {
            message.set("type", "tool");
            message.set("content", data.getStr("content"));
            message.set("tool_call_id", data.getStr("tool_call_id", ""));
            message.set("name", data.getStr("name", ""));
        }

        return message.isEmpty() ? null : JSONUtil.toJsonStr(message);
    }
}
