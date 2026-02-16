package com.example.hailuobackend.service;

import com.example.hailuobackend.model.VideoGenerateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class VideoService {

    @Value("${minimax.api.key:你的默认API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 提交视频生成任务
    public Map<String, Object> submitVideoTask(VideoGenerateRequest request) {
        String url = "https://api.minimaxi.com/v1/video_generation";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "video-01");
        body.put("prompt", request.getPrompt());

        if (request.getFirstFrame() != null && !request.getFirstFrame().isEmpty()) {
            body.put("first_frame_image", request.getFirstFrame());
        }
        if (request.getLastFrame() != null && !request.getLastFrame().isEmpty()) {
            body.put("last_frame_image", request.getLastFrame());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("提交视频生成任务失败: " + e.getMessage());
        }
    }

    // 2. 查询视频生成任务状态 (核心修复：自动换取真实的MP4链接)
    public Map<String, Object> queryVideoStatus(String taskId) {
        String url = "https://api.minimaxi.com/v1/query/video_generation?task_id=" + taskId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            // 【核心修复】：如果状态是成功，拿着 file_id 去换真实的 mp4 链接
            if (body != null && ("Success".equalsIgnoreCase((String) body.get("status")) || Integer.valueOf(3).equals(body.get("state")))) {
                String fileId = (String) body.get("file_id");
                if (fileId != null && !fileId.isEmpty()) {
                    try {
                        String fileUrl = "https://api.minimaxi.com/v1/files/retrieve?file_id=" + fileId;
                        ResponseEntity<Map> fileResponse = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, Map.class);
                        Map<String, Object> fileBody = fileResponse.getBody();

                        if (fileBody != null && fileBody.containsKey("file")) {
                            Map<String, Object> fileObj = (Map<String, Object>) fileBody.get("file");
                            String downloadUrl = (String) fileObj.get("download_url");
                            if (downloadUrl != null) {
                                // 将真实的 mp4 链接注入到返回结果中
                                body.put("video_url", downloadUrl);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("换取真实视频地址失败: " + ex.getMessage());
                    }
                }
            }
            return body;
        } catch (Exception e) {
            throw new RuntimeException("查询视频状态失败: " + e.getMessage());
        }
    }
}