package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.HealthRecord;

import com.example.petback.mapper.AnimalMapper;
import com.example.petback.mapper.HealthRecordMapper;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/health-record")
public class HealthRecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthRecordController.class);
    @Resource
    private HealthRecordMapper healthRecordMapper;
    @Resource
    private AnimalMapper petMapper;

    @Operation(summary = "添加健康记录")
    @PostMapping
    public Result<?> add(@RequestBody HealthRecord healthRecord) {
        int res = healthRecordMapper.insert(healthRecord);
        return res > 0? Result.success() : Result.error("-1", "添加失败");
    }
    @Operation(summary = "根据宠物id查询记录")
    @GetMapping("/selectByPetId/{id}")

    public Result<?> selectByPatId(@PathVariable Long id){
        QueryWrapper<HealthRecord> healthRecordQueryWrapper = new QueryWrapper<>();
        QueryWrapper<HealthRecord> petId = healthRecordQueryWrapper.eq("pet_id", id);
        List<HealthRecord> healthRecords = healthRecordMapper.selectList(healthRecordQueryWrapper);
        return Result.success(healthRecords);
    }
    @Operation(summary = "根据id删除健康记录")
    @DeleteMapping("/{id}")

    public Result<?> deleteById(@PathVariable Long id) {
        int res = healthRecordMapper.deleteById(id);
        return res > 0? Result.success() : Result.error("-1", "删除失败");
    }

    @Operation(summary = "批量删除健康记录")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        boolean flag = true;
        for (Long id : ids) {
            flag = healthRecordMapper.deleteById(id) > 0;
            if (!flag) break;
        }
        return flag? Result.success() : Result.error("-1", "（部分）删除失败");
    }

    @Operation(summary = "根据id更新健康记录")
    @PutMapping("/{id}")
    @Transactional
    public Result<?> updateById(@PathVariable Long id, @RequestBody HealthRecord healthRecord) {
        boolean flag = healthRecordMapper.updateById(healthRecord) > 0;
        return flag? Result.success() : Result.error("-1", "更新失败");
    }

    @Operation(summary = "根据id查询健康记录")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Long id) {
        HealthRecord healthRecord = healthRecordMapper.selectById(id);
        return healthRecord!= null? Result.success(healthRecord) : Result.error("-1", "查找失败");
    }

    @Operation(summary = "查询所有健康记录")
    @GetMapping("/selectAll")
    public Result<?> selectAll() {
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        List<HealthRecord> healthRecordList = healthRecordMapper.selectList(queryWrapper);
        return healthRecordList!= null? Result.success(healthRecordList) : Result.error("-1", "查找失败");
    }

    @Operation(summary = "获取健康记录")
    @GetMapping
    public Result<?> getHealthRecord(@RequestParam(defaultValue = "") String petId, @RequestParam(defaultValue = "") String recordDate) {
        LambdaQueryWrapper<HealthRecord> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(petId)) {
            queryWrapper.eq(HealthRecord::getPetId, Long.parseLong(petId));
        }
        if (StringUtils.isNotBlank(recordDate)) {
            queryWrapper.eq(HealthRecord::getRecordDate, LocalDate.parse(recordDate));
        }
        List<HealthRecord> healthRecordList = healthRecordMapper.selectList(queryWrapper);
        LOGGER.info("GET HEALTH RECORD: " + healthRecordList);
        return Result.success(healthRecordList);
    }

    // 分页查询健康记录
    @Operation(summary = "分页查询健康记录")
    @GetMapping("/page")
    public Result<?> getHealthRecordByPage(
            @RequestParam(defaultValue = "") String petName,
            @RequestParam(defaultValue = "") String recordDate,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("petName: " + petName + ", recordDate: " + recordDate + ", currentPage: " + currentPage + ", size: " + size);
        Page<HealthRecord> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<HealthRecord> queryWrapper = Wrappers.lambdaQuery();

        Page<HealthRecord> healthRecordPage = healthRecordMapper.selectByPage(petName,recordDate,page);
        return Result.success(healthRecordPage);
    }
}