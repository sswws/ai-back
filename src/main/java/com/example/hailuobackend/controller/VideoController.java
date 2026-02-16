package com.example.hailuobackend.controller;

import com.example.hailuobackend.model.VideoGenerateRequest;
import com.example.hailuobackend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // 提交视频生成任务
    @PostMapping("/generate")
    public ResponseEntity<?> generateVideo(@RequestBody VideoGenerateRequest request) {
        try {
            Map<String, Object> result = videoService.submitVideoTask(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 轮询查询视频生成状态
    @GetMapping("/status/{taskId}")
    public ResponseEntity<?> getVideoStatus(@PathVariable String taskId) {
        try {
            Map<String, Object> result = videoService.queryVideoStatus(taskId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}