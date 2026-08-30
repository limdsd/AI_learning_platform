package com.example.demo.ai;

import com.example.demo.common.BizException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedding 向量化客户端(OpenAI 兼容接口)
 *
 * 用于 RAG 相似题检索:把题干转成语义向量,再算余弦相似度。
 * 支持任意 OpenAI 兼容的 embedding 服务(硅基流动 / 智谱 / OpenAI 等),改配置即可。
 */
@Slf4j
@Component
public class EmbeddingClient {

    /** 单次请求最多向量化的文本条数,避免超长请求 */
    private static final int BATCH_SIZE = 32;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public EmbeddingClient(@Value("${embedding.api-key:}") String apiKey,
                           @Value("${embedding.base-url:}") String baseUrl,
                           @Value("${embedding.model:BAAI/bge-m3}") String model,
                           ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * 批量向量化:输入若干文本,返回与之一一对应的向量
     */
    public List<float[]> embed(List<String> texts) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("未配置 Embedding API Key,请设置环境变量 EMBEDDING_API_KEY");
        }
        List<float[]> result = new ArrayList<>();
        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            List<String> batch = texts.subList(i, Math.min(i + BATCH_SIZE, texts.size()));
            result.addAll(embedBatch(batch));
        }
        return result;
    }

    private List<float[]> embedBatch(List<String> texts) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", texts);

        try {
            String resp = restClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseEmbeddings(resp);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Embedding 服务失败", e);
            throw new BizException("调用 Embedding 服务失败: " + e.getMessage());
        }
    }

    private List<float[]> parseEmbeddings(String resp) {
        try {
            JsonNode root = objectMapper.readTree(resp);
            JsonNode data = root.path("data");
            List<JsonNode> items = new ArrayList<>();
            data.forEach(items::add);
            // 按 index 排序,保证与输入顺序一致
            items.sort(Comparator.comparingInt(n -> n.path("index").asInt()));
            List<float[]> result = new ArrayList<>();
            for (JsonNode item : items) {
                JsonNode emb = item.path("embedding");
                float[] vec = new float[emb.size()];
                for (int i = 0; i < emb.size(); i++) {
                    vec[i] = (float) emb.get(i).asDouble();
                }
                result.add(vec);
            }
            if (result.isEmpty()) {
                throw new BizException("Embedding 返回为空");
            }
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("解析 Embedding 返回失败: " + e.getMessage());
        }
    }
}
