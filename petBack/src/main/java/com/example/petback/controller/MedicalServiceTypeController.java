package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.MedicalServiceType;
import com.example.petback.mapper.MedicalServiceTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="医疗服务类型接口")
@RestController
@RequestMapping("/medicalServiceType")
public class MedicalServiceTypeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicalServiceTypeController.class);
    @Resource
    private MedicalServiceTypeMapper medicalServiceTypeMapper;

    @Operation(summary = "添加医疗服务类型")
    @PostMapping
    public Result<?> add(@RequestBody MedicalServiceType medicalServiceType) {
        medicalServiceTypeMapper.insert(medicalServiceType);
        return Result.success();
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        medicalServiceTypeMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int res = medicalServiceTypeMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    @Operation(summary = "更新医疗服务类型")
    @PutMapping("/{id}")
    public Result<?> updateMedicalServiceType(@PathVariable int id, @RequestBody MedicalServiceType medicalServiceType) {
        medicalServiceType.setId(id); // 确保更新时使用正确的ID
        LOGGER.info("PUT medicalServiceType ID: " + id + ", medicalServiceType: " + medicalServiceType);
        int res = medicalServiceTypeMapper.updateById(medicalServiceType);
        if (res > 0) {
            return Result.success(medicalServiceType);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        MedicalServiceType medicalServiceType = medicalServiceTypeMapper.selectById(id);
        if(medicalServiceType!=null){
            return Result.success(medicalServiceType);
        }else{
            return Result.error("-1","获取失败");
        }
    }

    @Operation(summary = "查询所有医疗服务类型")
    @GetMapping("/selectAll")
    public Result<?> selectAll() {
        LambdaQueryWrapper<MedicalServiceType> queryWrapper = Wrappers.lambdaQuery();
        List<MedicalServiceType> medicalServiceTypes = medicalServiceTypeMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL medicalServiceTypes: " + medicalServiceTypes);
        if (medicalServiceTypes != null && !medicalServiceTypes.isEmpty()) {
            return Result.success(medicalServiceTypes);
        } else {
            return Result.error("-1", "未找到服务类型");
        }
    }

    @Operation(summary = "分页查询医疗服务类型")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String typeName,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<MedicalServiceType> queryWrapper = Wrappers.lambdaQuery();
        LOGGER.info("pageNum:"+pageNum+"  pageSize:"+pageSize);
        if(typeName != null && !typeName.isEmpty()){
            queryWrapper.eq(MedicalServiceType::getName,typeName);
        }
        Page<MedicalServiceType> resultPage = medicalServiceTypeMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(resultPage);
    }
}