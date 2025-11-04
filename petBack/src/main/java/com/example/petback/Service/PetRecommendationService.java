package com.example.petback.Service;

import com.example.petback.entity.*;
import com.example.petback.mapper.*;
import com.example.petback.common.Enum.FosterStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

@Service
public class PetRecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetRecommendationService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private PetRecommendationMapper petRecommendationMapper;
    
    @Resource
    private AnimalMapper animalMapper;
    
    @Resource
    private GoodsMapper goodsMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PetTrainingServiceMapper petTrainingServiceMapper;
    
    @Resource
    private RestTemplate restTemplate;

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
    @Autowired
    private TrainingNameMapper trainingNameMapper;
    @Autowired
    private MedicalServiceTypeMapper medicalServiceTypeMapper;

    /**
     * 根据用户需求推荐宠物、宠物用品或服务
     * @param userId 用户ID，可选
     * @param userQuery 用户查询文本
     * @param type 要推荐的类型，可以是PET、GOODS、TRAINING、MEDICAL中的一种
     */
    public Map<String, Object> recommendByUserQuery(Integer userId, String userQuery, String type) {
        try {
            LOGGER.info("开始处理用户推荐请求，用户ID: {}, 查询: {}, 推荐类型: {}", userId, userQuery, type);
            
            // 设置默认推荐类型
            String recommendType = (type != null && !type.isEmpty()) ? type : "PET";
            
            LOGGER.info("最终推荐类型: {}", recommendType);
            
            // 1. 获取用户信息（可选）
            User user = null;
            if (userId != null) {
                user = userMapper.selectById(userId);
            }
            
            // 2. 根据用户选择的类型，只加载相应类型的数据
            List<Animal> availablePets = new ArrayList<>();
            List<Goods> availableGoods = new ArrayList<>();
            List<TrainingName> trainingServices = new ArrayList<>();
            List<MedicalServiceType> medicalServices = new ArrayList<>();
            
            // 根据推荐类型只加载对应的数据
            switch (recommendType) {
                // 在 recommendByUserQuery 方法中，改进 PET 类型的数据加载逻辑
                case "PET":
                    LambdaQueryWrapper<Animal> petQuery = Wrappers.<Animal>lambdaQuery()
                        .eq(Animal::getStatus, "待领养");
                    
                    // 智能关键词匹配 - 根据用户查询内容进行筛选
                    String queryLower = userQuery.toLowerCase();
                    
                    // 如果用户明确提到特定动物类型，进行精确筛选
                    if (queryLower.contains("猫") || queryLower.contains("cat") || queryLower.contains("喵")) {
                        petQuery.and(wrapper -> wrapper
                            .like(Animal::getBreed, "猫")
                            .or().like(Animal::getType, "猫")  // 同时检查type字段
                            .or().like(Animal::getName, "猫")
                            .or().like(Animal::getBehaviorTraits, "猫"));
                    } else if (queryLower.contains("狗") || queryLower.contains("dog") || queryLower.contains("犬") || queryLower.contains("汪")) {
                        petQuery.and(wrapper -> wrapper
                            .like(Animal::getBreed, "狗")
                            .or().like(Animal::getBreed, "犬")
                            .or().like(Animal::getType, "狗")  // 同时检查type字段
                            .or().like(Animal::getType, "犬")
                            .or().like(Animal::getName, "狗")
                            .or().like(Animal::getBehaviorTraits, "狗"));
                    } else if (queryLower.contains("鸟") || queryLower.contains("鹦鹉") || queryLower.contains("bird")) {
                        petQuery.and(wrapper -> wrapper
                            .like(Animal::getBreed, "鸟")
                            .or().like(Animal::getBreed, "鹦鹉")
                            .or().like(Animal::getType, "鸟")  // 同时检查type字段
                            .or().like(Animal::getName, "鸟")
                            .or().like(Animal::getBehaviorTraits, "鸟"));
                    }
                    
                    // 添加更细致的品种匹配
                    if (queryLower.contains("拉布拉多") || queryLower.contains("labrador")) {
                        petQuery.and(wrapper -> wrapper.like(Animal::getBreed, "拉布拉多"));
                    } else if (queryLower.contains("金毛") || queryLower.contains("golden")) {
                        petQuery.and(wrapper -> wrapper.like(Animal::getBreed, "金毛"));
                    } else if (queryLower.contains("英短") || queryLower.contains("英国短毛")) {
                        petQuery.and(wrapper -> wrapper.like(Animal::getBreed, "英国短毛"));
                    } else if (queryLower.contains("波斯") || queryLower.contains("persian")) {
                        petQuery.and(wrapper -> wrapper.like(Animal::getBreed, "波斯"));
                    }
                    
                    availablePets = animalMapper.selectList(petQuery);
                    LOGGER.info("根据用户查询 '{}' 筛选后的宠物数量: {}", userQuery, availablePets.size());
                    break;
                case "GOODS":
                    availableGoods = goodsMapper.selectList(
                        Wrappers.<Goods>lambdaQuery()
                            .gt(Goods::getNum, 0)  // 库存大于0的商品
                    );
                    break;
                case "TRAINING":
                    trainingServices = trainingNameMapper.selectList(
                        Wrappers.<TrainingName>lambdaQuery()
                            .isNotNull(TrainingName::getName)
                    );
                    break;
                case "MEDICAL":
                    medicalServices = medicalServiceTypeMapper.selectList(
                        Wrappers.<MedicalServiceType>lambdaQuery()
                            .isNotNull(MedicalServiceType::getName)
                    );
                    break;
                default:
                    // 如果类型不明确，则加载所有数据（向后兼容）
                    LOGGER.warn("未知的推荐类型: {}, 使用默认类型: PET", recommendType);
                    availablePets = animalMapper.selectList(
                        Wrappers.<Animal>lambdaQuery()
                            .eq(Animal::getStatus, "待领养")
                    );
                    recommendType = "PET"; // 重置为默认类型
                    break;
            }
            
            LOGGER.info("已加载数据: {}个宠物, {}个商品, {}个训练服务, {}个医疗服务",
                availablePets.size(), availableGoods.size(), 
                trainingServices.size(), medicalServices.size());
            
            // 3. 构建AI提示
            String prompt = buildRecommendationPrompt(user, userQuery, availablePets, availableGoods, 
               trainingServices, medicalServices);
            LOGGER.info("正在调用讯飞星火API，推荐提示: {}", prompt);

            // 4. 调用火星大模型API
            String aiResponse = callSparkApi(prompt);
            LOGGER.info("讯飞星火API响应: {}", aiResponse);
            
            // 5. 解析AI响应，提取推荐结果
            Map<String, Object> recommendationResult = parseRecommendationResponse(aiResponse, availablePets, availableGoods,
                                                          trainingServices, medicalServices);
            
            // 6. 保存推荐记录（可选）
            if (userId != null && recommendationResult.containsKey("recommendations")) {
                saveRecommendations(userId, userQuery, recommendationResult);
            }
            
            return recommendationResult;
        } catch (Exception e) {
            LOGGER.error("推荐过程中发生错误：{}", e.getMessage(), e);
            throw new RuntimeException("推荐失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 构建推荐系统的提示语
     */
    private String buildRecommendationPrompt(User user, String userQuery, List<Animal> availablePets, List<Goods> availableGoods,
                                         List<TrainingName> trainingServices,
                                            List<MedicalServiceType> medicalServices) {
        // 确定实际有哪些数据可供推荐
        boolean hasPets = !availablePets.isEmpty();
        boolean hasGoods = !availableGoods.isEmpty();
        boolean hasTrainingServices = !trainingServices.isEmpty();
        boolean hasMedicalServices = !medicalServices.isEmpty();
        
        StringBuilder petsInfo = new StringBuilder();
        StringBuilder goodsInfo = new StringBuilder();
        StringBuilder trainingInfo = new StringBuilder();
        StringBuilder medicalInfo = new StringBuilder();
        
        // 只构建用户选择类型的数据信息
        if (hasPets) {
            for (int i = 0; i < availablePets.size(); i++) {
                Animal pet = availablePets.get(i);
                petsInfo.append(String.format("宠物%d：ID=%d，名称=%s，品种=%s，年龄=%d，性别=%s，特征=%s\n", 
                    i+1, pet.getId(), pet.getName(), pet.getBreed(), pet.getAge(), pet.getSex(), pet.getBehaviorTraits()));
            }
        } else {
            petsInfo.append("当前没有可推荐的宠物\n");
        }
        
        if (hasGoods) {
            for (int i = 0; i < availableGoods.size(); i++) {
                Goods goods = availableGoods.get(i);
                goodsInfo.append(String.format("商品%d：ID=%d，名称=%s，价格=%.2f，描述=%s\n", 
                    i+1, goods.getId(), goods.getName(), goods.getPrice(), goods.getDesrc()));
            }
        } else {
            goodsInfo.append("当前没有可推荐的商品\n");
        }
        
        if (hasTrainingServices) {
            for (int i = 0; i < trainingServices.size(); i++) {
                TrainingName service = trainingServices.get(i);
                trainingInfo.append(String.format("训练服务%d：ID=%d，服务类型=%s,，描述=%s\n",
                    i+1, service.getId(), service.getName(),service.getDescription()));
            }
        } else {
            trainingInfo.append("当前没有可推荐的训练服务\n");
        }
        
        if (hasMedicalServices) {
            for (int i = 0; i < medicalServices.size(); i++) {
                MedicalServiceType service = medicalServices.get(i);
                medicalInfo.append(String.format("医疗服务%d：ID=%d，服务类型=%s，描述=%s\n", 
                    i+1, service.getId(), service.getName(), service.getDescription()));
            }
        } else {
            medicalInfo.append("当前没有可推荐的医疗服务\n");
        }
        
        String userContext = user != null ? 
            String.format("用户ID：%d，用户名：%s，性别：%s", user.getId(), user.getUsername(), user.getSex()) : 
            "未登录用户";
            
        // 构建提示文本前缀，明确指出用户选择的推荐类型
        StringBuilder promptPrefix = new StringBuilder("你是萌宠之家智能推荐系统，请根据用户需求");
        
        // 明确指定推荐类型
        if (hasPets && !hasGoods  && !hasTrainingServices && !hasMedicalServices) {
            promptPrefix.append("，仅推荐宠物。");
        } else if (!hasPets && hasGoods  && !hasTrainingServices && !hasMedicalServices) {
            promptPrefix.append("，仅推荐宠物用品。");
        } else if (!hasPets && !hasGoods && hasTrainingServices && !hasMedicalServices) {
            promptPrefix.append("，仅推荐宠物训练服务。");
        } else if (!hasPets && !hasGoods &&  !hasTrainingServices && hasMedicalServices) {
            promptPrefix.append("，仅推荐宠物医疗服务。");
        } else {
            // 多种类型可用时，列出所有可用类型
            promptPrefix.append("，推荐以下可用类型：");
            List<String> availableTypes = new ArrayList<>();
            if (hasPets) availableTypes.add("宠物");
            if (hasGoods) availableTypes.add("宠物用品");
            if (hasTrainingServices) availableTypes.add("宠物训练服务");
            if (hasMedicalServices) availableTypes.add("宠物医疗服务");
            promptPrefix.append(String.join("、", availableTypes));
        }
        
        promptPrefix.append("\n根据以下信息进行推荐：\n\n");
        
        // 构建完整提示文本，只包含用户选择类型的数据
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(promptPrefix.toString());
        fullPrompt.append("【用户信息】\n").append(userContext).append("\n\n");
        
        // 只包含有数据的部分
        if (hasPets) {
            fullPrompt.append("【可供推荐的宠物】\n").append(petsInfo.toString()).append("\n");
        }
        
        if (hasGoods) {
            fullPrompt.append("【可供推荐的商品】\n").append(goodsInfo.toString()).append("\n");
        }
        
        if (hasTrainingServices) {
            fullPrompt.append("【可供推荐的宠物训练服务】\n").append(trainingInfo.toString()).append("\n");
        }
        
        if (hasMedicalServices) {
            fullPrompt.append("【可供推荐的宠物医疗服务】\n").append(medicalInfo.toString()).append("\n");
        }
        
        // 在 buildRecommendationPrompt 方法中增强提示逻辑
fullPrompt.append("【用户需求分析】\n");
fullPrompt.append("用户查询: ").append(userQuery).append("\n");

// 调用用户意图分析方法
String intentAnalysis = analyzeUserIntent(userQuery);
fullPrompt.append("需求分析: ").append(intentAnalysis).append("\n\n");

fullPrompt.append("【推荐要求】\n");
fullPrompt.append("1. 请仔细分析用户的具体需求，优先推荐最匹配的选项\n");
fullPrompt.append("2. 如果用户提到特定动物类型（如猫、狗、鸟等），请优先推荐该类型\n");
fullPrompt.append("3. 推荐理由应该明确说明为什么该选项符合用户需求\n");
fullPrompt.append("4. 匹配度分数应该基于与用户需求的相关性进行评分\n");
fullPrompt.append("5. 如果没有完全匹配的选项，请在推荐理由中说明\n\n");
        
        // 强调只推荐用户选择类型的数据
        fullPrompt.append("请分析用户需求，仅从提供的数据类型中进行推荐。你的回复应包括推荐的宠物/商品/服务ID列表，推荐理由，以及匹配程度分数（0-100）。\n");
        fullPrompt.append("极其重要：\n");
        
        // 明确指出只推荐哪种类型
        if (hasPets && !hasGoods && !hasTrainingServices && !hasMedicalServices) {
            fullPrompt.append("1. 只推荐类型为PET的宠物，不要推荐其他类型\n");
        } else if (!hasPets && hasGoods && !hasTrainingServices && !hasMedicalServices) {
            fullPrompt.append("1. 只推荐类型为GOODS的商品，不要推荐其他类型\n");
        } else if (!hasPets && !hasGoods && hasTrainingServices && !hasMedicalServices) {
            fullPrompt.append("1. 只推荐类型为TRAINING的训练服务，不要推荐其他类型\n");
        } else if (!hasPets && !hasGoods && !hasTrainingServices && hasMedicalServices) {
            fullPrompt.append("1. 只推荐类型为MEDICAL的医疗服务，不要推荐其他类型\n");
        } else {
            fullPrompt.append("1. 只推荐上面列出的、有实际数据的选项类型\n");
        }
        
        fullPrompt.append(
            "2. 必须返回严格有效的JSON格式，所有引号必须使用英文双引号\n" +
            "3. id字段必须是数字，不能用文字描述或带引号，例如使用1而不是\"1\"或\"宠物ID\"\n" +
            "4. score字段必须是0-100之间的数字\n" +
            "5. 所有文本内容必须放在引号中\n" +
            "6. 只推荐上面提供的数据列表中实际存在的ID\n" +
            "7. 不要使用示例中的ID（如1、2、3、4、5、6）除非它们确实在上述列表中存在\n" +
            "8. id必须是数据中列出的真实ID数字，而不是位置编号或其他信息\n" +
            "9. 如果某种类型没有提供数据（显示为空列表），请不要推荐该类型\n"
        );
        
        // 添加回复格式要求
        fullPrompt.append(
            "回复格式要求如下（示例中的数字为实际ID值）：\n" +
            "```json\n" +
            "{\n" +
            "  \"recommendations\": [\n"
        );
        
        // 只包含有数据的类型示例
        if (hasPets) {
            fullPrompt.append(
                "    {\n" +
                "      \"type\": \"PET\",\n" + 
                "      \"id\": 1,\n" +
                "      \"reason\": \"推荐理由文本\",\n" +
                "      \"score\": 85\n" +
                "    },\n"
            );
        }
        
        if (hasGoods) {
            fullPrompt.append(
                "    {\n" +
                "      \"type\": \"GOODS\",\n" +
                "      \"id\": 2,\n" +
                "      \"reason\": \"推荐理由文本\",\n" +
                "      \"score\": 90\n" +
                "    },\n"
            );
        }
        
        if (hasTrainingServices) {
            fullPrompt.append(
                "    {\n" +
                "      \"type\": \"TRAINING\",\n" +
                "      \"id\": 4,\n" +
                "      \"serviceType\": \"基础训练\",\n" +
                "      \"reason\": \"推荐理由文本\",\n" +
                "      \"score\": 80\n" +
                "    },\n"
            );
        }
        
        if (hasMedicalServices) {
            fullPrompt.append(
                "    {\n" +
                "      \"type\": \"MEDICAL\",\n" +
                "      \"id\": 5,\n" +
                "      \"serviceType\": \"常规体检\",\n" +
                "      \"reason\": \"推荐理由文本\",\n" +
                "      \"score\": 70\n" +
                "    },\n"
            );
        }
        
        // 完成JSON格式
        fullPrompt.append(
            "  ],\n" +
            "  \"explanation\": \"整体推荐解释文本\"\n" +
            "}\n" +
            "```\n"
        );
        
        return fullPrompt.toString();
    }

    /**
     * 分析用户查询意图
     */
    private String analyzeUserIntent(String userQuery) {
        String queryLower = userQuery.toLowerCase();
        StringBuilder analysis = new StringBuilder();
        
        // 动物类型分析 - 更精确的匹配
        if (queryLower.contains("猫") || queryLower.contains("cat") || queryLower.contains("喵")) {
            analysis.append("用户明确想要猫类宠物；");
            
            // 具体品种分析
            if (queryLower.contains("英短") || queryLower.contains("英国短毛")) {
                analysis.append("偏好英国短毛猫；");
            } else if (queryLower.contains("波斯")) {
                analysis.append("偏好波斯猫；");
            } else if (queryLower.contains("缅因")) {
                analysis.append("偏好缅因猫；");
            }
        }
        
        if (queryLower.contains("狗") || queryLower.contains("dog") || queryLower.contains("犬") || queryLower.contains("汪")) {
            analysis.append("用户明确想要狗类宠物；");
            
            // 具体品种分析
            if (queryLower.contains("拉布拉多") || queryLower.contains("labrador")) {
                analysis.append("偏好拉布拉多犬；");
            } else if (queryLower.contains("金毛") || queryLower.contains("golden")) {
                analysis.append("偏好金毛寻回犬；");
            } else if (queryLower.contains("德牧") || queryLower.contains("德国牧羊")) {
                analysis.append("偏好德国牧羊犬；");
            } else if (queryLower.contains("比熊")) {
                analysis.append("偏好比熊犬；");
            }
        }
        
        if (queryLower.contains("鸟") || queryLower.contains("鹦鹉") || queryLower.contains("bird")) {
            analysis.append("用户想要鸟类宠物；");
        }
        
        // 年龄偏好分析
        if (queryLower.contains("幼") || queryLower.contains("小") || queryLower.contains("年轻")) {
            analysis.append("偏好年轻宠物；");
        }
        if (queryLower.contains("成年") || queryLower.contains("大")) {
            analysis.append("偏好成年宠物；");
        }
        
        // 性格特征分析
        if (queryLower.contains("活泼") || queryLower.contains("好动")) {
            analysis.append("偏好活泼好动的宠物；");
        }
        if (queryLower.contains("安静") || queryLower.contains("温顺")) {
            analysis.append("偏好安静温顺的宠物；");
        }
        
        return analysis.length() > 0 ? analysis.toString() : "用户需求较为宽泛，需要综合推荐";
    }
    
    /**
     * 调用讯飞星火API
     */
    private String callSparkApi(String prompt) {
        try {
            // 增强提示，强调需要返回JSON格式
            String enhancedPrompt = prompt + "\n\n请注意：你必须返回严格有效的JSON格式，不要添加额外的代码块标记或文本说明。";
            
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
            messageNode.put("content", enhancedPrompt);

            // 设置参数 - 减少创意性以获得更确定性和格式化的回答
            requestBody.put("temperature", 0.1);  // 降低温度以获得更确定性的回答
            requestBody.put("top_k", 1);          // 降低多样性
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 2048);  // 增加token上限以容纳更多回复内容

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            // 发送请求
            LOGGER.info("向讯飞星火发送请求，temperature=0.1，强调返回JSON格式");
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    String.class
            );

            // 解析响应
            JsonNode responseBody = objectMapper.readTree(responseEntity.getBody());
            String responseContent = responseBody.path("choices").path(0).path("message").path("content").asText();
            
            LOGGER.info("接收到星火API响应，长度：{} 字符", responseContent.length());
            
            // 预处理响应，尝试提取JSON
            if (responseContent.contains("```json")) {
                LOGGER.info("检测到响应包含JSON代码块");
                int startIndex = responseContent.indexOf("```json") + 7;
                int endIndex = responseContent.indexOf("```", startIndex);
                
                if (endIndex > startIndex) {
                    String jsonPart = responseContent.substring(startIndex, endIndex).trim();
                    try {
                        // 验证JSON有效性
                        objectMapper.readTree(jsonPart);
                        LOGGER.info("从响应中提取的JSON有效，直接返回JSON部分");
                        return jsonPart;
                    } catch (Exception e) {
                        LOGGER.warn("从响应中提取的JSON无效，返回完整响应供后续处理");
                    }
                }
            }
            
            return responseContent;
        } catch (Exception e) {
            LOGGER.error("调用讯飞星火API失败：{}", e.getMessage(), e);
            throw new RuntimeException("调用讯飞星火API失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解析AI回复，提取推荐结果
     */
    private Map<String, Object> parseRecommendationResponse(String aiResponse, List<Animal> availablePets, List<Goods> availableGoods,
                                                          List<TrainingName> trainingServices,
                                                           List<MedicalServiceType> medicalServices) {
        try {
            LOGGER.info("开始解析AI推荐回复");
            Map<String, Object> result = new HashMap<>();
            
            // 从AI回复中提取JSON部分
            String jsonStr = extractJsonFromResponse(aiResponse);
            LOGGER.info("提取的JSON字符串: {}", jsonStr);
            
            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            JsonNode recommendationsNode = rootNode.get("recommendations");
            String explanation = rootNode.has("explanation") ? rootNode.get("explanation").asText() : "根据您的需求，为您推荐以下选项";
            
            LOGGER.info("提取的解释: {}", explanation);
            
            List<Map<String, Object>> recommendations = new ArrayList<>();
            // 使用列表存储各类型的推荐项，而不是单个对象
            List<Map<String, Object>> petRecommendations = new ArrayList<>();
            List<Map<String, Object>> goodsRecommendations = new ArrayList<>();
            List<Map<String, Object>> trainingRecommendations = new ArrayList<>();
            List<Map<String, Object>> medicalRecommendations = new ArrayList<>();
            
            // 确定哪些类型有数据可供推荐
            boolean hasPets = !availablePets.isEmpty();
            boolean hasGoods = !availableGoods.isEmpty();
 
            boolean hasTraining = !trainingServices.isEmpty();
            boolean hasMedical = !medicalServices.isEmpty();
            
            // 处理推荐结果
            if (recommendationsNode != null && recommendationsNode.isArray()) {
                LOGGER.info("找到{}个推荐项", recommendationsNode.size());
                
                for (JsonNode recNode : recommendationsNode) {
                    try {
                        // 确保必要的字段存在
                        if (!recNode.has("type") || !recNode.has("id")) {
                            LOGGER.warn("推荐项缺少必要字段: {}", recNode);
                            continue;
                        }
                        
                        String type = recNode.get("type").asText();
                        LOGGER.info("处理类型为 {} 的推荐", type);
                        
                        // 检查是否为用户请求的类型以及是否有该类型的数据
                        if (("PET".equals(type) && !hasPets) ||
                            ("GOODS".equals(type) && !hasGoods) ||
                            ("TRAINING".equals(type) && !hasTraining) ||
                            ("MEDICAL".equals(type) && !hasMedical)) {
                            LOGGER.warn("跳过类型 {} 的推荐，因为用户未请求该类型或没有此类型的数据", type);
                            continue;
                        }
                        
                        // 检查ID是数字类型
                        if (!recNode.get("id").isNumber()) {
                            LOGGER.warn("推荐项的ID不是数字: {}", recNode.get("id"));
                            continue;
                        }
                        
                        int id = recNode.get("id").asInt();
                        String reason = recNode.has("reason") ? recNode.get("reason").asText() : "适合您的需求";
                        double score = recNode.has("score") ? recNode.get("score").asDouble() : 80.0;
                        
                        Map<String, Object> recMap = new HashMap<>();
                        recMap.put("type", type);
                        recMap.put("id", id);
                        recMap.put("reason", reason);
                        recMap.put("score", score);
                        
                        // 获取服务类型（如果有）
                        if (recNode.has("serviceType")) {
                            String serviceType = recNode.get("serviceType").asText();
                            recMap.put("serviceType", serviceType);
                        }
                        
                        // 添加详细信息
                        boolean foundDetails = false;
                        switch (type) {
                            case "PET":
                                if (!hasPets) {
                                    LOGGER.warn("用户未请求宠物推荐或没有可用宠物数据，跳过");
                                    continue;
                                }
                                for (Animal pet : availablePets) {
                                    if (pet.getId().equals(id)) {
                                        recMap.put("details", pet);
                                        foundDetails = true;
                                        LOGGER.info("找到匹配的宠物: ID={}, 名称={}", id, pet.getName());
                                        break;
                                    }
                                }
                                if (!foundDetails) {
                                    LOGGER.warn("未找到ID={}的宠物", id);
                                    // 尝试获取任意一个可用的宠物作为替代推荐
                                    if (!availablePets.isEmpty()) {
                                        Animal alternatePet = availablePets.get(0);
                                        recMap.put("details", alternatePet);
                                        recMap.put("id", alternatePet.getId());
                                        recMap.put("reason", "系统推荐：这是一个适合您的宠物");
                                        LOGGER.info("使用备选宠物推荐: ID={}, 名称={}", alternatePet.getId(), alternatePet.getName());
                                        foundDetails = true;
                                    } else {
                                        continue; // 跳过此推荐项
                                    }
                                }
                                // 将推荐项添加到宠物推荐列表
                                petRecommendations.add(recMap);
                                break;
                                
                            case "GOODS":
                                if (!hasGoods) {
                                    LOGGER.warn("用户未请求商品推荐或没有可用商品数据，跳过");
                                    continue;
                                }
                                for (Goods goods : availableGoods) {
                                    if (goods.getId().equals(id)) {
                                        recMap.put("details", goods);
                                        foundDetails = true;
                                        LOGGER.info("找到匹配的商品: ID={}, 名称={}", id, goods.getName());
                                        break;
                                    }
                                }
                                if (!foundDetails) {
                                    LOGGER.warn("未找到ID={}的商品", id);
                                    // 尝试获取任意一个可用的商品作为替代推荐
                                    if (!availableGoods.isEmpty()) {
                                        Goods alternateGoods = availableGoods.get(0);
                                        recMap.put("details", alternateGoods);
                                        recMap.put("id", alternateGoods.getId());
                                        recMap.put("reason", "系统推荐：这是一个适合您的商品");
                                        LOGGER.info("使用备选商品推荐: ID={}, 名称={}", alternateGoods.getId(), alternateGoods.getName());
                                        foundDetails = true;
                                    } else {
                                        continue; // 跳过此推荐项
                                    }
                                }
                                // 将推荐项添加到商品推荐列表
                                goodsRecommendations.add(recMap);
                                break;
                                
                            case "TRAINING":
                                if (!hasTraining) {
                                    LOGGER.warn("用户未请求训练服务推荐或没有可用训练服务数据，跳过");
                                    continue;
                                }
                                for (TrainingName service : trainingServices) {
                                    if (service.getId().equals(id)) {
                                        recMap.put("details", service);
                                        foundDetails = true;
                                        LOGGER.info("找到匹配的训练服务: ID={}, 类型={}", id, service.getName());
                                        break;
                                    }
                                }
                                if (!foundDetails) {
                                    LOGGER.warn("未找到ID={}的训练服务", id);
                                    // 尝试获取任意一个可用的训练服务作为替代推荐
                                    if (!trainingServices.isEmpty()) {
                                        TrainingName alternateService = trainingServices.get(0);
                                        recMap.put("details", alternateService);
                                        recMap.put("id", alternateService.getId());
                                        recMap.put("reason", "系统推荐：这是一个适合您的训练服务");
                                        LOGGER.info("使用备选训练服务推荐: ID={}, 类型={}", alternateService.getId(), alternateService.getName());
                                        foundDetails = true;
                                    } else {
                                        continue; // 跳过此推荐项
                                    }
                                }
                                // 将推荐项添加到训练服务推荐列表
                                trainingRecommendations.add(recMap);
                                break;
                                
                            case "MEDICAL":
                                if (!hasMedical) {
                                    LOGGER.warn("用户未请求医疗服务推荐或没有可用医疗服务数据，跳过");
                                    continue;
                                }
                                for (MedicalServiceType service : medicalServices) {
                                    if (service.getId().equals(id)) {
                                        recMap.put("details", service);
                                        foundDetails = true;
                                        LOGGER.info("找到匹配的医疗服务: ID={}, 类型={}", id, service.getName());
                                        break;
                                    }
                                }
                                if (!foundDetails) {
                                    LOGGER.warn("未找到ID={}的医疗服务", id);
                                    // 尝试获取任意一个可用的医疗服务作为替代推荐
                                    if (!medicalServices.isEmpty()) {
                                        MedicalServiceType alternateService = medicalServices.get(0);
                                        recMap.put("details", alternateService);
                                        recMap.put("id", alternateService.getId());
                                        recMap.put("reason", "系统推荐：这是一个适合您的医疗服务");
                                        LOGGER.info("使用备选医疗服务推荐: ID={}, 类型={}", alternateService.getId(), alternateService.getName());
                                        foundDetails = true;
                                    } else {
                                        continue; // 跳过此推荐项
                                    }
                                }
                                // 将推荐项添加到医疗服务推荐列表
                                medicalRecommendations.add(recMap);
                                break;
                                
                            default:
                                LOGGER.warn("未知的推荐类型: {}", type);
                                continue; // 跳过未知类型
                        }
                        
                        // 只添加找到详细信息的推荐项
                        recommendations.add(recMap);
                    } catch (Exception e) {
                        LOGGER.error("处理单个推荐项时出错: {}", e.getMessage());
                        // 跳过有问题的推荐项，继续处理其他推荐项
                    }
                }
            } else {
                LOGGER.warn("未找到推荐项数组或推荐数组为空");
            }
            
            LOGGER.info("成功解析出{}个有效推荐项", recommendations.size());
            
            result.put("recommendations", recommendations);
            result.put("explanation", explanation);
            
            // 返回每种类型的所有推荐项
            result.put("pets", petRecommendations);  // 改为复数名称，表示可能有多个
            result.put("goods", goodsRecommendations);
            result.put("training", trainingRecommendations);
            result.put("medical", medicalRecommendations);
            
            // 为了向后兼容，保留原来的单数键
            result.put("pet", petRecommendations.isEmpty() ? new HashMap<>() : petRecommendations.get(0));
            result.put("goodsItem", goodsRecommendations.isEmpty() ? new HashMap<>() : goodsRecommendations.get(0));
            result.put("trainingService", trainingRecommendations.isEmpty() ? new HashMap<>() : trainingRecommendations.get(0));
            result.put("medicalService", medicalRecommendations.isEmpty() ? new HashMap<>() : medicalRecommendations.get(0));
            
            return result;
        } catch (Exception e) {
            LOGGER.error("解析AI推荐回复失败：{}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "解析推荐结果失败: " + e.getMessage());
            errorResult.put("explanation", "抱歉，处理推荐结果时遇到了问题，请重新尝试");
            errorResult.put("recommendations", new ArrayList<>());
            errorResult.put("pets", new ArrayList<>());
            errorResult.put("goods", new ArrayList<>());
            errorResult.put("training", new ArrayList<>());
            errorResult.put("medical", new ArrayList<>());
            errorResult.put("pet", new HashMap<>());
            errorResult.put("goodsItem", new HashMap<>());
            errorResult.put("trainingService", new HashMap<>());
            errorResult.put("medicalService", new HashMap<>());
            return errorResult;
        }
    }
    
    /**
     * 从AI回复中提取JSON格式的内容
     */
    private String extractJsonFromResponse(String response) {
        try {
            LOGGER.info("AI原始回复: {}", response);
            
            // 首先尝试从代码块中提取
            // 寻找 ```json 和 ``` 之间的内容
            int startIndex = response.indexOf("```json");
            if (startIndex != -1) {
                startIndex += 7; // 跳过 ```json
                int endIndex = response.indexOf("```", startIndex);
                if (endIndex != -1) {
                    String jsonContent = response.substring(startIndex, endIndex).trim();
                    LOGGER.info("从代码块中提取的JSON: {}", jsonContent);
                    try {
                        // 验证提取的内容是否为有效JSON
                        objectMapper.readTree(jsonContent);
                        LOGGER.info("从代码块中提取的有效JSON: {}", jsonContent);
                        return jsonContent;
                    } catch (Exception e) {
                        LOGGER.warn("代码块中的JSON无效: {}", e.getMessage());
                        // 尝试修复常见问题
                        String fixedJson = fixJsonContent(jsonContent);
                        try {
                            // 再次验证修复后的JSON是否有效
                            objectMapper.readTree(fixedJson);
                            LOGGER.info("修复后的JSON有效: {}", fixedJson);
                            return fixedJson;
                        } catch (Exception e2) {
                            LOGGER.warn("修复后的JSON仍然无效: {}", e2.getMessage());
                        }
                    }
                }
            }
            
            // 如果代码块提取失败，尝试提取普通的JSON对象
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}");
            
            // 如果找到了完整的JSON对象
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                String potentialJson = response.substring(jsonStart, jsonEnd + 1);
                try {
                    // 尝试解析，验证是否为有效JSON
                    objectMapper.readTree(potentialJson);
                    LOGGER.info("找到有效JSON: {}", potentialJson);
                    return potentialJson;
                } catch (Exception e) {
                    LOGGER.warn("直接提取的JSON无效: {}", e.getMessage());
                    // 尝试修复常见问题
                    String fixedJson = fixJsonContent(potentialJson);
                    try {
                        // 再次验证修复后的JSON是否有效
                        objectMapper.readTree(fixedJson);
                        LOGGER.info("修复后的JSON有效: {}", fixedJson);
                        return fixedJson;
                    } catch (Exception e2) {
                        LOGGER.warn("修复后的JSON仍然无效: {}", e2.getMessage());
                    }
                }
            }
            
            // 如果还没找到有效的JSON，尝试任何代码块
            startIndex = response.indexOf("```");
            if (startIndex != -1) {
                startIndex += 3; // 跳过 ```
                int endIndex = response.indexOf("```", startIndex);
                if (endIndex != -1) {
                    String blockContent = response.substring(startIndex, endIndex).trim();
                    // 跳过可能的语言标识，如 json、javascript 等
                    if (blockContent.contains("\n")) {
                        blockContent = blockContent.substring(blockContent.indexOf("\n")).trim();
                    }
                    LOGGER.info("从通用代码块中提取的内容: {}", blockContent);
                    
                    // 查找块内容中的JSON对象
                    jsonStart = blockContent.indexOf("{");
                    jsonEnd = blockContent.lastIndexOf("}");
                    if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                        String potentialJson = blockContent.substring(jsonStart, jsonEnd + 1);
                        try {
                            objectMapper.readTree(potentialJson);
                            LOGGER.info("从代码块中找到有效JSON: {}", potentialJson);
                            return potentialJson;
                        } catch (Exception e) {
                            LOGGER.warn("代码块中的JSON无效: {}", e.getMessage());
                            String fixedJson = fixJsonContent(potentialJson);
                            try {
                                objectMapper.readTree(fixedJson);
                                LOGGER.info("修复后的代码块JSON有效: {}", fixedJson);
                                return fixedJson;
                            } catch (Exception e2) {
                                LOGGER.warn("修复后的代码块JSON仍然无效: {}", e2.getMessage());
                            }
                        }
                    }
                }
            }
            
            // 尝试提取和修复整个响应作为最后的尝试
            try {
                // 移除所有的代码块标记和可能的Markdown语法
                String cleanedResponse = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .replaceAll("\\*\\*", "")
                    .trim();
                    
                // 尝试找到JSON部分
                jsonStart = cleanedResponse.indexOf("{");
                jsonEnd = cleanedResponse.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    String potentialJson = cleanedResponse.substring(jsonStart, jsonEnd + 1);
                    String fixedJson = fixJsonContent(potentialJson);
                    objectMapper.readTree(fixedJson);
                    LOGGER.info("清理后的响应JSON有效: {}", fixedJson);
                    return fixedJson;
                }
            } catch (Exception e) {
                LOGGER.warn("无法修复整体响应JSON: {}", e.getMessage());
            }
            
            // 如果上述方法都失败了，返回一个空的推荐结果
            LOGGER.error("无法从AI回复中提取有效JSON，返回空结果");
            return "{ \"recommendations\": [], \"explanation\": \"无法解析推荐结果\" }";
        } catch (Exception e) {
            LOGGER.error("提取JSON过程中发生错误: {}", e.getMessage());
            return "{ \"recommendations\": [], \"explanation\": \"处理推荐结果时出错\" }";
        }
    }
    
    /**
     * 尝试修复常见的JSON格式问题
     */
    private String fixJsonContent(String jsonContent) {
        LOGGER.info("尝试修复JSON内容");
        
        // 替换非标准的ID值表示
        String fixedJson = jsonContent;
        
        // 查找并替换非数字ID（如"id": 宠物实际ID 或 "id": "1"）
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*([^0-9,\\}\\]].+?)([,\\}\\]])");
        Matcher idMatcher = idPattern.matcher(fixedJson);
        StringBuffer sb = new StringBuffer();
        
        while (idMatcher.find()) {
            String invalidIdValue = idMatcher.group(1).trim();
            String endChar = idMatcher.group(2);
            LOGGER.info("发现无效的ID值: {}", invalidIdValue);
            
            // 尝试从无效值中提取数字
            Pattern numberPattern = Pattern.compile("(\\d+)");
            Matcher numberMatcher = numberPattern.matcher(invalidIdValue);
            
            if (numberMatcher.find()) {
                String extractedNumber = numberMatcher.group(1);
                LOGGER.info("从无效ID中提取出数字: {}", extractedNumber);
                idMatcher.appendReplacement(sb, "\"id\": " + extractedNumber + endChar);
            } else {
                // 如果无法提取数字，使用默认值1
                LOGGER.info("无法从无效ID中提取数字，使用默认值1");
                idMatcher.appendReplacement(sb, "\"id\": 1" + endChar);
            }
        }
        idMatcher.appendTail(sb);
        fixedJson = sb.toString();
        
        // 移除引号括起的数字（如"id": "123"）
        fixedJson = fixedJson.replaceAll("\"id\"\\s*:\\s*\"(\\d+)\"", "\"id\": $1");
        
        // 修复缺失引号的文本字段
        Pattern textPattern = Pattern.compile("\"(reason|explanation|serviceType)\"\\s*:\\s*([^\"\\{\\}\\[\\]]+?)([,\\}\\]])");
        Matcher textMatcher = textPattern.matcher(fixedJson);
        sb = new StringBuffer();
        
        while (textMatcher.find()) {
            String field = textMatcher.group(1);
            String value = textMatcher.group(2).trim();
            String endChar = textMatcher.group(3);
            
            if (!value.startsWith("\"") || !value.endsWith("\"")) {
                LOGGER.info("修复字段 {} 的值: {}", field, value);
                // 确保文本被引号括起
                textMatcher.appendReplacement(sb, "\"" + field + "\": \"" + value.replace("\"", "") + "\"" + endChar);
            }
        }
        textMatcher.appendTail(sb);
        fixedJson = sb.toString();
        
        LOGGER.info("JSON修复结果: {}", fixedJson);
        return fixedJson;
    }
    
    /**
     * 保存推荐记录
     */
    private void saveRecommendations(Integer userId, String userQuery, Map<String, Object> recommendationResult) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) recommendationResult.get("recommendations");
            
            if (recommendations != null) {
                for (Map<String, Object> rec : recommendations) {
                    PetRecommendation recommendation = new PetRecommendation();
                    recommendation.setUserId(userId);
                    recommendation.setUserQuery(userQuery);
                    recommendation.setRecommendationType((String) rec.get("type"));
                    recommendation.setRecommendedItemId((Integer) rec.get("id"));
                    recommendation.setReason((String) rec.get("reason"));
                    recommendation.setScore((Double) rec.get("score"));
                    // 保存服务类型（如果有）
                    if (rec.containsKey("serviceType")) {
                        recommendation.setServiceType((String) rec.get("serviceType"));
                    }
                    recommendation.setCreatedAt(LocalDateTime.now());
                    
                    petRecommendationMapper.insert(recommendation);
                }
            }
        } catch (Exception e) {
            LOGGER.error("保存推荐记录失败：{}", e.getMessage(), e);
            // 不中断主流程，仅记录错误
        }
    }
}