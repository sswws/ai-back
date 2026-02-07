package com.example.hailuobackend.model;

import lombok.Data;

@Data
public class GenerateRequest {
    // 提示词
    private String prompt;

    // 模型ID (前端传 image-pro 等，后端转为 image-01)
    private String modelId;

    // 生成数量
    private int count;

    // 分辨率/比例 (如 1:1, 16:9)
    private String ratio;

    // 参考图 (Base64格式，用于图生图)
    private String refImage;
}