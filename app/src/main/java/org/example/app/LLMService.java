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
    // 请确保这里的 API Key 是有效的
    private static final String API_KEY = "sk-e813807975e54dad9197c946964c424a"; 
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private final HttpClient httpClient;
    private final Gson gson;

    public LLMService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    //  模式一：绘图指令生成 
    public CompletableFuture<String> generateDSL(String userPrompt, String currentGraphState) {
        // [修改] 在提示词中强调这是无向图上下文
        String systemPrompt = """
            你是一个图数据结构可视化助手。用户会提供【当前图的状态】（DSL格式）和【操作指令】。
            请判断用户的意图，并输出相应的 DSL 指令。

            === 核心上下文 ===
            **这是一个无向图环境**。当用户要求连接两个节点时，只需生成一条指令即可。

            === 核心规则 ===
            1. 如果用户说"创建"、"新建"、"清空"，请在 DSL 第一行输出 `RESET`。
            2. 输出变更指令：u -> v : w (表示连接 u 和 v) 或 DEL u -> v 或 DEL NODE u。
            3. 格式要求：绘图指令必须以 `[DSL]` 开头。如果无法执行绘图，请以 `[MSG]` 开头回复。
            
            === 当前图的状态 ===
            """ + (currentGraphState.isEmpty() ? "(空图)" : currentGraphState);

        return sendRequest(systemPrompt, userPrompt);
    }

    //  模式二：图论深度分析与问答 
    public CompletableFuture<String> chatWithGraph(String userPrompt, String currentGraphState) {
        // [修改] 这里的提示词是关键，增加了“核心定义”板块
        String systemPrompt = """
            你是一位精通数据结构与算法的计算机教授。
            用户会提供当前【图的DSL结构】，请你基于这个图回答用户的问题。

            === 核心定义（至关重要） ===
            **这是一个无向图 (Undirected Graph)**。
            尽管 DSL 格式使用了 `u -> v` 这种箭头表示法，但在本场景中，它仅代表 u 和 v 之间有一条连线。
            **请严格遵守以下逻辑：**
            1. **忽略方向**：`0 -> 1` 意味着 0 和 1 互相连通。不要分析“入度”或“出度”，只分析“度”。
            2. **路径分析**：如果存在 `A -> B`，则可以从 A 走到 B，也可以从 B 走到 A。
            3. **连通性**：判断强连通分量时，请按无向图的连通分量逻辑处理。

            === 你的能力 ===
            1. 结构分析：判断图是稀疏还是稠密？是否有环？是否连通？有没有孤立点？
            2. 算法推演：如果用户问 Dijkstra，你可以预判路径；如果问着色问题，分析最少颜色数。
            3. 教学指导：用通俗易懂的语言解释图的性质。

            === 回复要求 ===
            - **重要：请严格使用 DSL 中提供的节点 ID（如 0, 1, 2）进行指代，严禁将其转换为字母（如 A, B, C）。**
            - 直接输出分析内容，**不要**输出 `[DSL]` 或 `[MSG]` 前缀。
            - 格式清晰，可以使用列表或加粗。
            
            === 当前图的状态 ===
            """ + (currentGraphState == null || currentGraphState.isEmpty() ? "(空图)" : currentGraphState);

        return sendRequest(systemPrompt, userPrompt);
    }

    // 通用请求发送方法
    private CompletableFuture<String> sendRequest(String systemContent, String userContent) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "deepseek-chat");
        requestBody.addProperty("temperature", 0.4);

        JsonArray messages = new JsonArray();
        
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemContent);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userContent);
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
            // 增加空指针保护
            if (!json.has("choices") || json.getAsJsonArray("choices").size() == 0) {
                 return "[MSG] API 返回空结果";
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