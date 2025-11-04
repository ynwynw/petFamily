package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.Result;
import com.example.petback.entity.PetMedicalService;
import com.example.petback.mapper.PetMedicalServiceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name="宠物医疗服务接口")
@RestController
@RequestMapping("/petMedicalService")
public class PetMedicalServiceController {
    @Resource
    private PetMedicalServiceMapper petMedicalServiceMapper;

    @Operation(summary = "添加医疗服务")
    @PostMapping
    public Result<?> add(@RequestBody PetMedicalService petMedicalService) {
        // 检查医疗服务名称是否已存在
        petMedicalService.setStatus("未开始");
        petMedicalService.setCreatedAt(LocalDate.now());
        int res = petMedicalServiceMapper.insert(petMedicalService);
        return res>0?Result.success():Result.error("-1","添加失败");
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = petMedicalServiceMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = petMedicalServiceMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }

    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    public Result<?> updateById(@PathVariable int id,@RequestBody PetMedicalService petMedicalService) {
        int res = petMedicalServiceMapper.updateById(petMedicalService);
        return res>0?Result.success():Result.error("-1","更新失败");
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public  Result<?> selectById(@PathVariable Integer id) {
        PetMedicalService petMedicalService = petMedicalServiceMapper.selectById(id);
        return petMedicalService!=null?Result.success(petMedicalService):Result.error("-1","查找失败");
    }

    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public   Result<?> selectAll() {
        QueryWrapper<PetMedicalService> petMedicalServiceQueryWrapper = new QueryWrapper<>();
        List<PetMedicalService> petMedicalServices = petMedicalServiceMapper.selectList(petMedicalServiceQueryWrapper);
        return petMedicalServices!=null?Result.success(petMedicalServices):Result.error("-1","查找失败");
    }

    @Operation(summary = "分页查询医疗服务信息")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String petName,
                                @RequestParam(defaultValue = "")String username,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<PetMedicalService> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(petName)){
            queryWrapper.eq(PetMedicalService::getPetName,petName);
        }
        if(StringUtils.isNotBlank(username)){
            queryWrapper.eq(PetMedicalService::getUsername,username);
        }
        Page<PetMedicalService> ResultPage = petMedicalServiceMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(ResultPage);
    }
}