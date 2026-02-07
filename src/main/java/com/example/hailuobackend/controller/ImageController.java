package com.example.hailuobackend.controller;

import com.example.hailuobackend.model.GenerateRequest;
import com.example.hailuobackend.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*") // 允许前端跨域
public class ImageController {

    @Autowired
    private ImageService imageService;

    // --- 生成图片接口 ---
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody GenerateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> images = imageService.generateImage(
                    request.getPrompt(),
                    request.getRatio(),
                    request.getRefImage()
            );
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", images);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // --- 图片下载代理接口 ---
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadImage(@RequestParam String url) {
        System.out.println("【后端日志】收到下载请求: " + url); // 关键日志：看到这就说明接口通了
        try {
            URL imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
            // 伪装浏览器 User-Agent，防止阿里云 OSS 拦截
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            InputStream inputStream = conn.getInputStream();

            HttpHeaders headers = new HttpHeaders();
            // 强制下载，文件名设为时间戳
            headers.add("Content-Disposition", "attachment; filename=hailuo_ai_" + System.currentTimeMillis() + ".jpg");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            System.err.println("下载失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}