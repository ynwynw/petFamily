package com.example.petback.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Enum.AdoptEnum;
import com.example.petback.common.Enum.AnimalEnum;
import com.example.petback.common.Result;
import com.example.petback.entity.Adopt;
import com.example.petback.entity.Animal;
import com.example.petback.mapper.AdoptMapper;
import com.example.petback.mapper.AnimalMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "宠物领养接口")
@RestController
@RequestMapping("/adopt")
public class AdoptController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdoptController.class);
    @Resource
    private AdoptMapper adoptMapper;
    @Resource
    private AnimalMapper animalMapper;

    @Operation(summary = "添加记录")
    @PostMapping
    public Result<?> add(@RequestBody Adopt adopt) {
        adopt.setTime(DateUtil.now());
        // 修改状态为审核中
        adopt.setStatus(AdoptEnum.PENDING_REVIEW.getInfo());
        Integer animalId = adopt.getAnimalId();
        Animal animal = animalMapper.selectById(animalId);
        // 宠物状态设置为审核中
        animal.setStatus(AdoptEnum.PENDING_REVIEW.getInfo());
        int update = animalMapper.updateById(animal);
        if (update <= 0) {
            return Result.error("-1", "添加失败");
        }
        int insert = adoptMapper.insert(adopt);
        if (insert <= 0) {
            return Result.error("-1", "添加失败");
        }
        return Result.success();
    }

    @Operation(summary = "审核领养申请")
    @PutMapping("/review/{id}")
    public Result<?> reviewAdopt(@PathVariable int id, @RequestBody Adopt adopt) {
        adopt.setId(id);
        adopt.setReviewTime(DateUtil.now());
        
        Adopt existAdopt = adoptMapper.selectById(id);
        if (existAdopt == null) {
            return Result.error("-1", "领养记录不存在");
        }
        
        // 获取宠物信息
        Animal animal = animalMapper.selectById(existAdopt.getAnimalId());
        if (animal == null) {
            return Result.error("-1", "宠物不存在");
        }
        
        // 根据审核结果更新宠物状态
        if (AdoptEnum.ADOPTING.getInfo().equals(adopt.getStatus())) {
            // 审核通过，状态变为领养中
            animal.setStatus(AnimalEnum.ADOPTING.getInfo());
        } else if (AdoptEnum.REJECTED.getInfo().equals(adopt.getStatus())) {
            // 审核不通过，状态恢复为待领养
            animal.setStatus(AnimalEnum.NO_ADOPT.getInfo());
        }
        
        // 更新宠物状态
        int updateAnimal = animalMapper.updateById(animal);
        if (updateAnimal <= 0) {
            return Result.error("-1", "更新宠物状态失败");
        }
        
        // 更新领养记录
        int updateAdopt = adoptMapper.updateById(adopt);
        if (updateAdopt <= 0) {
            return Result.error("-1", "更新领养记录失败");
        }
        
        return Result.success();
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        Boolean i = adoptMapper.deleteById(id) > 0;
        if (i) {
            return Result.success();
        }
        return Result.error("-1", "删除失败");
    }


    @Operation(summary = "批量删除记录")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int count = 0;
        for (Integer id : ids) {
            int i = adoptMapper.deleteById(id);
            if (i > 0) {
                count++;
            }


        }
        if (count != ids.size()) {
            return Result.error("-1", "部分删除失败");
        }
        return Result.success();
    }


    @Operation(summary = "修改记录")
    @PutMapping("/{id}")
    public Result<?> updateById(@PathVariable int id, @RequestBody Adopt adopt) {
        adopt.setId(id);
        if (AnimalEnum.ADOPT_CANCEL.getInfo().equals(adopt.getStatus())) {
            // 已归还我们需要把宠物信息的状态同步成 待领养
            Animal animal = animalMapper.selectById(adopt.getAnimalId());
            animal.setStatus(AnimalEnum.NO_ADOPT.getInfo());
            animalMapper.updateById(animal);
        }
        int i = adoptMapper.updateById(adopt);
        if (i > 0) {
            return Result.success();
        }
        return Result.error("-1", "更新失败");
    }


    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        Adopt adopt = adoptMapper.selectById(id);
        if (adopt != null) {
            return Result.success(adopt);
        } else {
            return Result.error("-1", "获取失败");
        }
    }

    @Operation(summary = "查询所有记录")
    @GetMapping("/selectAll")
    public Result<?> selectAll(Adopt adopt) {
        List<Adopt> adopts = adoptMapper.selectAll(adopt);
        if (adopt != null) {
            return Result.success(adopts);
        } else {
            return Result.error("-1", "获取失败");
        }
    }

    @Operation(summary = "获取用户的领养申请")
    @GetMapping("/user/{userId}")
    public Result<?> getUserAdopts(@PathVariable Integer userId) {
        List<Adopt> adopts = adoptMapper.selectByUserId(userId);
        return Result.success(adopts);
    }

    @Operation(summary = "分页查询领养记录")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "1") Integer currentPage,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Adopt> page = new Page<>(currentPage, size);
        Page<Adopt> adoptPage = adoptMapper.selectByPage(name, page);
        LOGGER.info("SELECT PAGE:" + adoptPage.getRecords());
        return Result.success(adoptPage);
    }
}
