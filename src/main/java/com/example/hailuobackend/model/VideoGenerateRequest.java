package com.example.hailuobackend.model;

import lombok.Data;

@Data
public class VideoGenerateRequest {
    private String prompt;
    private String modelId;
    private String firstFrame; // 首帧图片
    private String lastFrame;  // 尾帧图片
}