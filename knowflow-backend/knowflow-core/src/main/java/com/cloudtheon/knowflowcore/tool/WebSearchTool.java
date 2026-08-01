package com.cloudtheon.knowflowcore.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Web 搜索工具
 * <p>
 * 使用 DuckDuckGo Instant Answer API（免费、无需 API Key）获取实时搜索结果摘要。
 * 用于补充模型训练截止时间之后的实时信息。
 * </p>
 */
@Component
public class WebSearchTool {

    private static final int TIMEOUT_MS = 10000;
    private static final int MAX_RESULTS = 5;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "在互联网上搜索信息，返回相关结果（标题、摘要、链接）。用于获取实时信息或最新资料")
    public String webSearch(
            @ToolParam(description = "搜索关键词，例如：Spring Boot 4 新特性") String query) {
        if (query == null || query.isBlank()) {
            return "错误：搜索关键词为空";
        }
        HttpURLConnection conn = null;
        try {
            String urlStr = "https://api.duckduckgo.com/?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&format=json&no_html=1&skip_disambig=1";
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (KnowFlow)");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                return "搜索失败：HTTP " + conn.getResponseCode();
            }

            JsonNode root;
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                root = objectMapper.readTree(r);
            }

            List<String> results = new ArrayList<>();
            String abstractText = root.path("AbstractText").asText("");
            String abstractUrl = root.path("AbstractURL").asText("");
            if (!abstractText.isEmpty()) {
                results.add("摘要: " + abstractText + (abstractUrl.isEmpty() ? "" : "\n链接: " + abstractUrl));
            }

            JsonNode related = root.path("RelatedTopics");
            if (related.isArray()) {
                for (JsonNode node : related) {
                    if (results.size() >= MAX_RESULTS) {
                        break;
                    }
                    String text = node.path("Text").asText("");
                    String firstUrl = node.path("FirstURL").asText("");
                    if (text.isEmpty()) {
                        // 可能是一组分组的 Topics
                        JsonNode topics = node.path("Topics");
                        if (topics.isArray()) {
                            for (JsonNode t : topics) {
                                if (results.size() >= MAX_RESULTS) {
                                    break;
                                }
                                String tText = t.path("Text").asText("");
                                String tUrl = t.path("FirstURL").asText("");
                                if (!tText.isEmpty()) {
                                    results.add("- " + tText + (tUrl.isEmpty() ? "" : "\n  " + tUrl));
                                }
                            }
                        }
                        continue;
                    }
                    results.add("- " + text + (firstUrl.isEmpty() ? "" : "\n  " + firstUrl));
                }
            }

            if (results.isEmpty()) {
                return "未找到与「" + query + "」直接相关的信息，建议换个关键词。";
            }
            return "「" + query + "」的搜索结果：\n" + String.join("\n", results);
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage() + "（请检查网络或稍后重试）";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
