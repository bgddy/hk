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

    public CompletableFuture<String> generateDSL(String userPrompt) {
        String systemPrompt = """
            你是一个图数据结构可视化助手。
            请判断用户的输入是【绘图指令】还是【普通对话】，并按以下规则输出：

            情况1：如果用户想要创建、修改图（如"画个三角形"、"创建5个点"、"随机权重的环"）
            请以 `[DSL]` 开头，后跟严格的 DSL 代码。
            
            DSL 语法规则：
            1. 有向边带权重：u -> v : w  (注意：冒号前后要有空格，w必须是整数)
            2. 有向边默认权重：u -> v      (仅当用户未指定权重且未要求随机权重时使用)
            3. 【重要】如果用户提到"随机权重"、"带权"或"权值为x"，必须在 DSL 中生成具体的 : w 部分。
            4. 【非常重要】顶点编号必须从 0 开始！例如 5 个点应该是 0,1,2,3,4。不要使用 1,2,3,4,5。
            5. 不要包含 markdown 代码块符号（如 ```）。

            情况2：如果用户是在打招呼、询问身份或其他闲聊（如"你是谁"、"你好"）
            请以 `[MSG]` 开头，后跟简短的回复文本。

            示例输入1："创建一个三角形，边权为10"
            示例输出1：
            [DSL]
            0 -> 1 : 10
            1 -> 2 : 10
            2 -> 0 : 10

            示例输入2："创建一个包含4个点的环，权重随机"
            示例输出2：
            [DSL]
            0 -> 1 : 5
            1 -> 2 : 12
            2 -> 3 : 8
            3 -> 0 : 3

            示例输入3："你是谁？"
            示例输出3：
            [MSG] 我是你的图论可视化助手，我可以帮你绘制各种图结构。请告诉我你想要什么样的图。
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