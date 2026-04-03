package com.example.paper.service;

import com.example.paper.config.AppProperties;
import com.example.paper.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMService {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public LLMService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public List<String> classify(String content, List<String> candidateCategories) {
        if (content == null) {
            return Collections.emptyList();
        }
        if (candidateCategories == null || candidateCategories.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "未配置分类，无法执行 LLM 分类。请先添加分类。");
        }

        String apiKey = appProperties.getLlm().getApiKey();
        String endpoint = appProperties.getLlm().getEndpoint();

        if (apiKey == null || apiKey.isBlank() || endpoint == null || endpoint.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LLM API Key 未配置，请先在 application.yml 中完善 app.llm.api-key。");
        }

        String prompt = buildPrompt(content, candidateCategories);
        try {
            String responseBody = callDashScope(prompt, apiKey, endpoint);
            List<String> parsed = parseCategories(responseBody, candidateCategories);
            if (parsed.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM 分类结果为空，请检查提示词或模型返回格式。");
            }
            return parsed;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM 分类调用失败：" + e.getMessage());
        }
    }

    private String callDashScope(String prompt, String apiKey, String endpoint) throws IOException, InterruptedException {
        String targetUrl = endpoint;
        String body;

        // 兼容 OpenAI-compatible 模式：
        // endpoint = https://dashscope.aliyuncs.com/compatible-mode/v1
        // 实际调用路径应为 /chat/completions
        if (isCompatibleMode(endpoint)) {
            if (!targetUrl.endsWith("/chat/completions")) {
                targetUrl = trimTrailingSlash(targetUrl) + "/chat/completions";
            }

            var root = objectMapper.createObjectNode();
            root.put("model", appProperties.getLlm().getModel());
            var messages = objectMapper.createArrayNode();
            messages.add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", prompt));
            root.set("messages", messages);
            root.put("temperature", 0.1);
            body = root.toString();
        } else {
            // DashScope 原生 generation 形态
            var root = objectMapper.createObjectNode();
            root.put("model", appProperties.getLlm().getModel());

            var input = objectMapper.createObjectNode();
            input.put("prompt", prompt);
            root.set("input", input);

            var params = objectMapper.createObjectNode();
            params.put("result_format", appProperties.getLlm().getResponseFormat());
            root.set("parameters", params);
            body = root.toString();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofSeconds(30))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String brief = response.body() == null ? "" : response.body();
            if (brief.length() > 180) {
                brief = brief.substring(0, 180) + "...";
            }
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "LLM API Key 无效或权限不足，请更新 app.llm.api-key。response=" + brief);
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM call failed, status=" + response.statusCode() + ", response=" + brief);
        }
        return response.body();
    }

    private String buildPrompt(String content, List<String> candidateCategories) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是推荐系统领域专家，请根据论文内容判断其属于以下哪些类别（可以多选）：\n");
        for (int i = 0; i < candidateCategories.size(); i++) {
            sb.append(i + 1).append(". ").append(candidateCategories.get(i)).append("\n");
        }
        sb.append("\n返回格式（只能返回 JSON 数组，不要添加解释）：\n");
        sb.append("[\"").append(candidateCategories.get(0)).append("\"]\n\n");
        sb.append("论文内容如下：\n").append(content);
        return sb.toString();
    }

    private List<String> parseCategories(String responseBody, List<String> candidateCategories) {
        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }

        // 先从 JSON 结构里找可能的 text 字段
        String text = extractTextFromJson(responseBody);
        if (text == null) {
            text = responseBody;
        }

        // 抽取形如 ["CF Based","Graph Based"] 的数组片段
        Pattern p = Pattern.compile("\\[[^\\]]*\\]");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String arr = m.group();
            try {
                List<String> parsed = objectMapper.readValue(arr, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                return normalize(parsed, candidateCategories);
            } catch (Exception ignored) {
                // fallthrough
            }
        }

        // 兜底：直接匹配候选类别名出现情况
        Set<String> results = new HashSet<>();
        for (String c : candidateCategories) {
            if (text.contains(c)) {
                results.add(c);
            }
        }
        return new ArrayList<>(results);
    }

    private String extractTextFromJson(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // 常见：output.text 或 output.choices[0].message.content
            if (root.has("output") && root.get("output").has("text")) {
                return root.get("output").get("text").asText();
            }
            if (root.has("output") && root.get("output").has("choices")) {
                JsonNode choices = root.get("output").get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode first = choices.get(0);
                    if (first.has("message") && first.get("message").has("content")) {
                        return first.get("message").get("content").asText();
                    }
                }
            }
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode first = root.get("choices").get(0);
                if (first.has("message") && first.get("message").has("content")) {
                    return first.get("message").get("content").asText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> normalize(List<String> parsed, List<String> candidateCategories) {
        if (parsed == null) {
            return Collections.emptyList();
        }
        Set<String> allowed = new HashSet<>(candidateCategories);
        List<String> out = new ArrayList<>();
        for (String s : parsed) {
            if (s != null) {
                String trimmed = s.trim();
                if (allowed.contains(trimmed)) {
                    out.add(trimmed);
                }
            }
        }
        return out;
    }

    private boolean isCompatibleMode(String endpoint) {
        if (endpoint == null) return false;
        String e = endpoint.toLowerCase();
        return e.contains("/compatible-mode/") || e.endsWith("/v1") || e.contains("/v1/");
    }

    private String trimTrailingSlash(String s) {
        if (s == null) return "";
        String out = s;
        while (out.endsWith("/")) out = out.substring(0, out.length() - 1);
        return out;
    }
}

