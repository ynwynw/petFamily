package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.PetNotification;
import com.example.petback.entity.User;
import com.example.petback.mapper.PetNotificationMapper;
import com.example.petback.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "通知接口")
@RestController
@RequestMapping("/pet-notification")
public class PetNotificationController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PetNotificationController.class);
    
    @Resource
    private PetNotificationMapper petNotificationMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Operation(summary = "向指定用户发送通知")
    @PostMapping("/user/{userId}")
    public Result<?> addForUser(@PathVariable Integer userId, @RequestBody PetNotification notification) {
        notification.setUserId(userId);
        notification.setTimestamp(LocalDateTime.now());
        notification.setStatus("unread");
        int res = petNotificationMapper.insert(notification);
        return res > 0 ? Result.success() : Result.error("-1", "添加失败");
    }
    
    @Operation(summary = "向指定角色的所有用户发送通知")
    @PostMapping("/role/{role}")
    @Transactional
    public Result<?> addForRole(@PathVariable String role, @RequestBody PetNotification notification) {
        // 查找指定角色的所有用户
        LambdaQueryWrapper<User> userWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getRole, role);
        List<User> users = userMapper.selectList(userWrapper);
        
        if (users.isEmpty()) {
            return Result.error("-1", "未找到该角色的用户");
        }
        
        // 为每个用户创建通知
        notification.setTimestamp(LocalDateTime.now());
        notification.setStatus("unread");
        
        for (User user : users) {
            notification.setId(null); // 确保每次插入都是新记录
            notification.setUserId(user.getId());
            petNotificationMapper.insert(notification);
        }
        
        return Result.success();
    }
    
    @Operation(summary = "标记通知为已读")
    @PutMapping("/markAsRead/{id}")
    public Result<?> markAsRead(@PathVariable Integer id) {
        PetNotification notification = new PetNotification();
        notification.setId(id);
        notification.setStatus("read");
        int res = petNotificationMapper.updateById(notification);
        return res > 0 ? Result.success() : Result.error("-1", "更新失败");
    }
    
    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = petNotificationMapper.deleteById(id);
        return res > 0 ? Result.success() : Result.error("-1", "删除失败");
    }
    
    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int res = petNotificationMapper.deleteBatchIds(ids);
        return res > 0 ? Result.success() : Result.error("-1", "删除失败");
    }
    
    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        PetNotification notification = petNotificationMapper.selectById(id);
        if (notification != null) {
            User user = userMapper.selectById(notification.getUserId());
            if (user != null) {
                notification.setUsername(user.getUsername());
            }
        }
        return notification != null ? Result.success(notification) : Result.error("-1", "查找失败");
    }
    
    @Operation(summary = "分页查询通知")
    @GetMapping("/page")
    public Result<?> selectPage(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 先查询符合用户名的用户ID列表
        List<Integer> userIds = null;
        if (StringUtils.isNotBlank(username)) {
            LambdaQueryWrapper<User> userWrapper = Wrappers.<User>lambdaQuery()
                    .like(User::getUsername, username);
            userIds = userMapper.selectList(userWrapper)
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                // 如果没找到匹配的用户，直接返回空结果
                return Result.success(new Page<>(currentPage, size));
            }
        }
        
        // 构建通知查询条件
        LambdaQueryWrapper<PetNotification> wrapper = Wrappers.<PetNotification>lambdaQuery()
                .in(userIds != null, PetNotification::getUserId, userIds)
                .eq(StringUtils.isNotBlank(status), PetNotification::getStatus, status)
                .orderByDesc(PetNotification::getTimestamp);
        
        // 执行分页查询
        Page<PetNotification> page = petNotificationMapper.selectPage(
                new Page<>(currentPage, size), wrapper);
        
        // 填充用户名
        page.getRecords().forEach(notification -> {
            User user = userMapper.selectById(notification.getUserId());
            if (user != null) {
                notification.setUsername(user.getUsername());
            }
        });
        
        return Result.success(page);
    }
    
    @Operation(summary = "获取用户的通知列表")
    @GetMapping("/user/{userId}")
    public Result<?> getNotificationsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 构建查询条件
        LambdaQueryWrapper<PetNotification> wrapper = Wrappers.<PetNotification>lambdaQuery()
                .eq(PetNotification::getUserId, userId)
                .eq(StringUtils.isNotBlank(status), PetNotification::getStatus, status)
                .orderByDesc(PetNotification::getTimestamp);
        
        // 执行分页查询
        Page<PetNotification> page = petNotificationMapper.selectPage(
                new Page<>(currentPage, size), wrapper);
        
        // 填充用户名
        page.getRecords().forEach(notification -> {
            User user = userMapper.selectById(notification.getUserId());
            if (user != null) {
                notification.setUsername(user.getUsername());
            }
        });
        
        return Result.success(page);
    }
} 