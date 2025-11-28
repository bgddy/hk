package org.example.app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LLMService {

    private static final String API_KEY = "sk-e813807975e54dad9197c946964c424a"; 
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private final HttpClient httpClient;
    private final Gson gson;

    public LLMService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<String> generateDSL(String userPrompt, String currentGraphState) {
        String systemPrompt = """
            你是一个图数据结构可视化助手。用户会提供【当前图的状态】（DSL格式）和【操作指令】。
            请判断用户的意图，并输出相应的 DSL 指令或对话。

            === 核心规则 ===
            1. **新建 vs 增量**：
               - 如果用户说"创建"、"新建"、"画一个..."，请在 DSL 第一行输出 `RESET`，然后输出新图的边。
               - 如果用户说"添加"、"连接"、"删除"，则**不要**输出 `RESET`，直接输出变更指令。
            2. **DSL 语法**：
               - 重置画布：RESET
               - 添加/更新边：u -> v : w  (w必须是整数，如果用户说"随机权值"，请你自己生成一个1-20的随机整数填入w)
               - 删除边：DEL u -> v
               - 删除点：DEL NODE u
            3. **格式要求**：
               - 绘图指令以 `[DSL]` 开头。
               - 普通对话以 `[MSG]` 开头。
               - 不要使用 Markdown 代码块。
            
            === 当前图的状态 ===
            """ + (currentGraphState.isEmpty() ? "(空图)" : currentGraphState) + """
            
            === 示例 ===
            用户："创建一个三角形，权值随机"
            [DSL]
            RESET
            0 -> 1 : 5
            1 -> 2 : 12
            2 -> 0 : 8

            用户："删除点 0" (假设当前有图)
            [DSL]
            DEL NODE 0
            """;

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("temperature", 0.4); 

        JsonArray messages = new JsonArray();
        
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        requestBody.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseResponse);
    }

    private String parseResponse(String jsonResponse) {
        try {
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            if (json.has("error")) {
                return "[MSG] API 错误: " + json.get("error").getAsJsonObject().get("message").getAsString();
            }
            String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            
            return content.replaceAll("```dsl", "")
                          .replaceAll("```", "")
                          .trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "[MSG] 解析错误: " + e.getMessage();
        }
    }
}