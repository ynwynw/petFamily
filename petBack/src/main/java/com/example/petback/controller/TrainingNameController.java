package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.TrainingName;
import com.example.petback.mapper.TrainingNameMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="训练名称接口")
@RestController
@RequestMapping("/trainingName")
public class TrainingNameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingNameController.class);
    @Resource
    private TrainingNameMapper trainingNameMapper;

    @Operation(summary = "添加训练名称")
    @PostMapping
    public Result<?> add(@RequestBody TrainingName trainingName) {
        trainingNameMapper.insert(trainingName);
        return Result.success();
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        trainingNameMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int res = trainingNameMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    @Operation(summary = "更新训练名称")
    @PutMapping("/{id}")
    public Result<?> updateTrainingName(@PathVariable int id, @RequestBody TrainingName trainingName) {
        trainingName.setId(id); // 确保更新时使用正确的ID
        LOGGER.info("PUT trainingName ID: " + id + ", trainingName: " + trainingName);
        int res = trainingNameMapper.updateById(trainingName);
        if (res > 0) {
            return Result.success(trainingName);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        TrainingName trainingName = trainingNameMapper.selectById(id);
        if(trainingName!=null){
            return Result.success(trainingName);
        }else{
            return Result.error("-1","获取失败");
        }
    }

    @Operation(summary = "查询所有训练名称")
    @GetMapping("/selectAll")
    public Result<?> selectAll(TrainingName trainingName ) {
        LambdaQueryWrapper<TrainingName> queryWrapper = Wrappers.lambdaQuery();
        List<TrainingName> trainingNames = trainingNameMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL trainingNames: " + trainingNames);
        if (trainingNames!= null &&!trainingNames.isEmpty()) {
            return Result.success(trainingNames);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "分页查询训练名称")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<TrainingName> queryWrapper = Wrappers.lambdaQuery();
        LOGGER.info("pageNum:"+pageNum+"  pageSize:"+pageSize);
        if(StringUtils.isNotBlank(name)){
            queryWrapper.eq(TrainingName::getName,name);
        }
        Page<TrainingName> ResultPage = trainingNameMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(ResultPage);
    }
}