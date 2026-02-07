package com.example.hailuobackend.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Value("${minimax.api.key}")
    private String apiKey;

    @Value("${minimax.api.url}")
    private String apiUrl;

    public List<String> generateImage(String prompt, String ratio, String refImage) {
        // 1. 构建请求头
        // MiniMax 要求 Authorization: Bearer <API_KEY>
        String authHeader = "Bearer " + apiKey;

        // 2. 构建请求体 (JSON)
        JSONObject body = new JSONObject();
        body.put("model", "image-01"); // MiniMax 目前的图片模型代号
        body.put("prompt", prompt);
        body.put("n", 1); // 每次生成1张 (如果需要多张可以改这里，或者循环调用)

        // 处理比例 (默认 1:1)
        // MiniMax 支持: "1:1", "16:9", "4:3", "3:2", "2:3", "3:4", "9:16", "21:9"
        String finalRatio = StrUtil.isBlank(ratio) || "Auto".equals(ratio) ? "1:1" : ratio;
        body.put("aspect_ratio", finalRatio);

        // 3. 处理图生图 (如果有参考图)
        if (StrUtil.isNotBlank(refImage)) {
            // 前端传来的 Base64 通常带前缀 "data:image/png;base64,"，需要去掉
            String cleanBase64 = refImage;
            if (refImage.contains(",")) {
                cleanBase64 = refImage.split(",")[1];
            }
            // MiniMax 图生图参数，通常是 "image" 或 "init_image" 接收 Base64
            // 根据文档，image-01 接受 "image" 字段作为参考图
            body.put("image", cleanBase64);
            // 提示优化 (可选，默认开启)
            body.put("prompt_optimizer", true);
        }

        System.out.println("正在请求 MiniMax API...");
        System.out.println("Prompt: " + prompt);

        // 4. 发送 POST 请求
        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(60000) // 设置超时 60秒，因为生成图片很慢
                    .execute();

            String resStr = response.body();
            System.out.println("MiniMax 响应: " + resStr);

            if (!response.isOk()) {
                throw new RuntimeException("API请求失败: " + response.getStatus() + " - " + resStr);
            }

            // 5. 解析结果
            // 假设返回格式: { "base_resp": {...}, "images": [ { "base64": "..." } ] }
            // 或者 { "id": "...", "data": { "image_urls": ["..."] } }
            // 注意：MiniMax 不同版本返回格式可能不同，这里按标准 image-01 格式解析

            JSONObject resJson = JSONUtil.parseObj(resStr);

            // 检查是否有错误
            JSONObject baseResp = resJson.getJSONObject("base_resp");
            if (baseResp != null && baseResp.getInt("status_code") != 0) {
                throw new RuntimeException("API错误: " + baseResp.getStr("status_msg"));
            }

            List<String> imageUrls = new ArrayList<>();

            // 情况A：返回的是 base64 (常见于 image-01)
            JSONArray images = resJson.getJSONArray("images");
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    JSONObject imgObj = images.getJSONObject(i);
                    String base64 = imgObj.getStr("base64");
                    // 补全前缀让前端能显示
                    imageUrls.add("data:image/png;base64," + base64);
                }
            }
            // 情况B：返回的是 url (data.image_urls)
            else if (resJson.containsKey("data")) {
                JSONArray urls = resJson.getJSONObject("data").getJSONArray("image_urls");
                if (urls != null) {
                    imageUrls = urls.toList(String.class);
                }
            }

            if (imageUrls.isEmpty()) {
                throw new RuntimeException("API返回成功但没有图片数据");
            }

            return imageUrls;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成失败: " + e.getMessage());
        }
    }
}