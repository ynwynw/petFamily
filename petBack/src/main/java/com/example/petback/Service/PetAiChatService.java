package com.example.petback.Service;

import com.example.petback.entity.Animal;
import com.example.petback.entity.PetAiChat;
import com.example.petback.mapper.AnimalMapper;
import com.example.petback.mapper.PetAiChatMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PetAiChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetAiChatService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private PetAiChatMapper petAiChatMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private AnimalMapper animalMapper;

    @Value("${spark.app-id}")
    private String appId;

    @Value("${spark.api-key}")
    private String apiKey;

    @Value("${spark.api-secret}")
    private String apiSecret;

    @Value("${spark.api-url}")
    private String apiUrl;

    @Value("${spark.model}")
    private String model;

    @Value("${spark.api-password}")
    private String apiPassword;

    public PetAiChat chat(Integer userId, Integer petId, String message) {
        try {
            // 查询宠物信息
            Animal pet = animalMapper.selectById(petId);
            if (pet == null) {
                throw new RuntimeException("宠物不存在");
            }

            // 构建AI提示
            String prompt = buildPrompt(pet, message);
            LOGGER.info("正在调用讯飞星火API，用户问题: {}", prompt);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiPassword);

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("user", "user_" + System.currentTimeMillis());

            // 添加消息
            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode messageNode = messages.addObject();
            messageNode.put("role", "user");
            messageNode.put("content", prompt);

            // 设置参数
            requestBody.put("temperature", 0.7);
            requestBody.put("top_k", 4);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 1024);

            LOGGER.info("请求体: {}", requestBody.toString());
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            // 发送请求
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    String.class
            );

            // 解析响应
            JsonNode responseBody = objectMapper.readTree(responseEntity.getBody());
            LOGGER.info("完整响应: {}", responseBody.toString());
            String aiResponse = responseBody.path("choices").path(0).path("message").path("content").asText();
            LOGGER.info("讯飞星火API响应: {}", aiResponse);

            // 创建聊天记录对象
            PetAiChat chatRecord = new PetAiChat();
            chatRecord.setUserId(userId);
            chatRecord.setPetId(petId);
            chatRecord.setMessage(message);
            chatRecord.setResponse(aiResponse);
            chatRecord.setCreatedAt(LocalDateTime.now());

            // 保存聊天记录到数据库
            petAiChatMapper.insert(chatRecord);

            return chatRecord;
        } catch (Exception e) {
            LOGGER.error("调用讯飞星火API失败：{}", e.getMessage(), e);
            throw new RuntimeException("调用讯飞星火API失败：" + e.getMessage(), e);
        }
    }

    private String buildPrompt(Animal pet, String userMessage) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        
        return String.format(
                "当前时间是：%s。你现在是一只名叫%s的%s岁%s%s。性格特征是：%s。" +
                        "请以宠物的身份回复以下消息，如果用户询问时间相关问题，请基于当前时间回答：%s",
                currentTime,
                pet.getName(),
                pet.getAge(),
                pet.getSex(),
                pet.getBreed(),
                pet.getBehaviorTraits(),
                userMessage
        );
    }
}