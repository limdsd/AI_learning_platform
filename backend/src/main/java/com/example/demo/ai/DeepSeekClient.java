package com.example.demo.ai;

import com.example.demo.common.BizException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 大模型客户端(OpenAI 兼容接口)
 */
@Slf4j
@Component
public class DeepSeekClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public DeepSeekClient(@Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.base-url}") String baseUrl,
                          @Value("${deepseek.model}") String model,
                          ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * 调用 DeepSeek 对话接口
     *
     * @param system   系统提示词
     * @param user     用户提示词
     * @param jsonMode 是否强制 JSON 输出
     * @return 模型回复内容
     */
    public String chat(String system, String user, boolean jsonMode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置 DeepSeek API Key,请设置环境变量 DEEPSEEK_API_KEY");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        ));
        body.put("temperature", 0.7);
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        try {
            String resp = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return extractContent(resp);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek 失败", e);
            throw new BizException("调用 AI 服务失败: " + e.getMessage());
        }
    }

    private String extractContent(String resp) {
        try {
            JsonNode root = objectMapper.readTree(resp);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new BizException("AI 返回内容为空");
            }
            return content.asText();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("解析 AI 返回内容失败: " + e.getMessage());
        }
    }
}
