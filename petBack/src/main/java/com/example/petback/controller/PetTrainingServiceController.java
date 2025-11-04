package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;

import com.example.petback.entity.PetTrainingService;
import com.example.petback.mapper.PetTrainingServiceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="宠物训练服务接口")
@RestController
@RequestMapping("/petTrainingService")
public class PetTrainingServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetTrainingServiceController.class);
    @Resource
    private PetTrainingServiceMapper petTrainingServiceMapper;

    @Operation(summary = "添加宠物训练服务")
    @PostMapping
    public Result<?> add(@RequestBody PetTrainingService petTrainingService) {
        petTrainingService.setCompletionStatus("未开始");
        petTrainingServiceMapper.insert(petTrainingService);
        return Result.success();
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        petTrainingServiceMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int res = petTrainingServiceMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    @Operation(summary = "更新宠物训练服务")
    @PutMapping("/{id}")
    public Result<?> updatePetTrainingService(@PathVariable int id, @RequestBody PetTrainingService petTrainingService) {
        petTrainingService.setId(id); // 确保更新时使用正确的ID
        LOGGER.info("PUT petTrainingService ID: " + id + ", petTrainingService: " + petTrainingService);
        int res = petTrainingServiceMapper.updateById(petTrainingService);
        if (res > 0) {
            return Result.success(petTrainingService);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        PetTrainingService petTrainingService = petTrainingServiceMapper.selectById(id);
        if(petTrainingService!=null){
            return Result.success(petTrainingService);
        }else{
            return Result.error("-1","获取失败");
        }

    }

    @Operation(summary = "查询所有宠物训练服务")
    @GetMapping("/selectAll")
    public Result<?> selectAll(PetTrainingService petTrainingService ) {
        LambdaQueryWrapper<PetTrainingService> queryWrapper = Wrappers.lambdaQuery();
        List<PetTrainingService> petTrainingServices = petTrainingServiceMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL petTrainingServices: " + petTrainingServices);
        if (petTrainingServices!= null &&!petTrainingServices.isEmpty()) {
            return Result.success(petTrainingServices);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "分页查询宠物训练服务")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String serviceType,
                                @RequestParam(defaultValue = "")String username,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<PetTrainingService> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(serviceType)){
            queryWrapper.eq(PetTrainingService::getServiceType,serviceType);
        }
        if(StringUtils.isNotBlank(username)){
            queryWrapper.eq(PetTrainingService::getUserName,username);
        }
        Page<PetTrainingService> ResultPage = petTrainingServiceMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(ResultPage);
    }
}