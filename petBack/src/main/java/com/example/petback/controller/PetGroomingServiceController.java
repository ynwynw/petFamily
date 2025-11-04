package com.example.petback.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.Result;
import com.example.petback.entity.PetGroomingService;
import com.example.petback.mapper.PetGroomingServiceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name="宠物美容服务接口")
@RestController
@RequestMapping("/petGroomingService")
public class PetGroomingServiceController {
    @Resource
    private PetGroomingServiceMapper petGroomingServiceMapper;

    @Operation(summary = "添加美容服务")
    @PostMapping
    public Result<?> add(@RequestBody PetGroomingService petGroomingService) {
        // 检查美容服务名称是否已存在
        petGroomingService.setStatus("未开始");
        petGroomingService.setCreatedAt(LocalDate.from(LocalDateTime.now()));
        int res = petGroomingServiceMapper.insert(petGroomingService);
        return res>0?Result.success():Result.error("-1","添加失败");
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = petGroomingServiceMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = petGroomingServiceMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }

    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    public Result<?> updateById(@PathVariable int id,@RequestBody PetGroomingService petGroomingService) {
        int res = petGroomingServiceMapper.updateById(petGroomingService);
        return res>0?Result.success():Result.error("-1","更新失败");
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public  Result<?> selectById(@PathVariable Integer id) {
        PetGroomingService petGroomingService = petGroomingServiceMapper.selectById(id);
        return petGroomingService!=null?Result.success(petGroomingService):Result.error("-1","查找失败");
    }

    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public   Result<?> selectAll() {
        QueryWrapper<PetGroomingService> petGroomingServiceQueryWrapper = new QueryWrapper<>();
        List<PetGroomingService> petGroomingServices = petGroomingServiceMapper.selectList(petGroomingServiceQueryWrapper);
        return petGroomingServices!=null?Result.success(petGroomingServices):Result.error("-1","查找失败");
    }

    @Operation(summary = "分页查询美容服务信息")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String petName,
                                @RequestParam(defaultValue = "")String username,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<PetGroomingService> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(petName)){
            queryWrapper.eq(PetGroomingService::getPetName,petName);
        }
        if(StringUtils.isNotBlank(username)){
            queryWrapper.eq(PetGroomingService::getUsername,username);
        }
        Page<PetGroomingService> ResultPage = petGroomingServiceMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(ResultPage);
    }
}