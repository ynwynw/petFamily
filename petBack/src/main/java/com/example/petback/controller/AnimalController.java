package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.Animal;
import com.example.petback.entity.User;
import com.example.petback.mapper.AnimalMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;
@Tag(name="宠物接口")
@RestController
@RequestMapping("/animal")
public class AnimalController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Resource
    private AnimalMapper animalMapper;
    @Operation(summary = "添加宠物")
    @PostMapping
    public Result<?> add(@RequestBody Animal animal) {
        animalMapper.insert(animal);

        return Result.success();
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        animalMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int res = animalMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }



    @Operation(summary = "更新宠物信息")
    @PutMapping("/{id}")
    public Result<?> updateAnimal(@PathVariable int id, @RequestBody  Animal animal) {
        animal.setId(id); // 确保更新时使用正确的ID
        LOGGER.info("PUT animal ID: " + id + ", animal: " + animal);
        int res = animalMapper.updateById(animal);
        if (res > 0) {
            return Result.success(animal);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {

        Animal animal = animalMapper.selectById(id);
        if(animal!=null){
            return Result.success(animal);
        }else{
            return Result.error("-1","获取失败");
        }

    }

    @Operation(summary = "查询所有宠物")
    @GetMapping("/selectAll")
    public Result<?> selectAll(Animal animal ) {
        LambdaQueryWrapper<Animal> queryWrapper = Wrappers.lambdaQuery();
        List<Animal> animals = animalMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL animals: " + animals);
        if (animals != null && !animals.isEmpty()) {
            return Result.success(animals);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "分页查询动物")
    @GetMapping("/page")
    public Result<?> selectPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 构建查询条件
        LambdaQueryWrapper<Animal> wrapper = Wrappers.<Animal>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Animal::getName, name)
                .like(StringUtils.isNotBlank(type), Animal::getType, type)
                .orderByDesc(Animal::getId);
        
        // 执行分页查询
        Page<Animal> page = animalMapper.selectPage(new Page<>(currentPage, size), wrapper);
        return Result.success(page);
    }
}
