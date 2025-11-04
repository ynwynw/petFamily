package com.example.petback.controller;

import com.example.petback.Service.PetAiChatService;
import com.example.petback.common.Result;
import com.example.petback.entity.PetAiChat;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/pet/ai")
public class PetAiChatController {

    @Resource
    private PetAiChatService petAiChatService;

    @PostMapping("/chat")
    public Result<PetAiChat> chat(@RequestParam Integer userId,
                                  @RequestParam Integer petId,
                                  @RequestParam String message) {
        return Result.success(petAiChatService.chat(userId, petId, message));
    }
} 