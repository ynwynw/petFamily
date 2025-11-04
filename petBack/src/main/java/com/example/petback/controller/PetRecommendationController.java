package com.example.petback.controller;

import com.example.petback.Service.PetRecommendationService;
import com.example.petback.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
public class PetRecommendationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetRecommendationController.class);

    @Resource
    private PetRecommendationService recommendationService;

    @Operation(summary = "基于用户查询推荐宠物和宠物用品")
    @PostMapping("/query")
    public Result<?> recommendByQuery(@RequestParam(required = false) Integer userId,
                                      @RequestParam String query,
                                      @RequestParam(required = false) String type) {
        LOGGER.info("收到推荐请求：userId={}, query={}, type={}", userId, query, type);
        try {
            Map<String, Object> recommendations = recommendationService.recommendByUserQuery(userId, query, type);
            return Result.success(recommendations);
        } catch (Exception e) {
            LOGGER.error("推荐处理失败", e);
            return Result.error("-1", "推荐处理失败：" + e.getMessage());
        }
    }
} 