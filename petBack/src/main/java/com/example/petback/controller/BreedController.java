package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Enum.RoomStatus;
import com.example.petback.common.Result;
import com.example.petback.entity.Breed;
import com.example.petback.entity.Room;
import com.example.petback.mapper.BreedMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "品种管理类")
@RequestMapping("/breed")
@RestController
public class BreedController {
    @Resource
    BreedMapper breedMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(BreedController.class);

    @Operation(summary = "添加品种")
    @PostMapping
    public Result<?> add(@RequestBody Breed breed) {
        // 检查房间名称是否已存在
        if (breedMapper.selectCountByName(breed.getBreedName()) > 0) {
            return Result.error("-1","品种名称已存在，请使用其他名称。");
        }

        int res = breedMapper.insert(breed);
        return res>0?Result.success():Result.error("-1","添加失败");
    }
    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = breedMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        LOGGER.info("DELE BATCH:"+ids);
        boolean flag =true;
        for (Integer id : ids) {
            flag = breedMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }
    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    public Result<?> updateById(@PathVariable int id,@RequestBody Breed breed) {
        int res = breedMapper.updateById(breed);
        return res>0?Result.success():Result.error("-1","更新失败");
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public  Result<?> selectById(@PathVariable Integer id) {
        Breed breed = breedMapper.selectById(id);
        return breed!=null?Result.success(breed):Result.error("-1","查找失败");
    }
    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public   Result<?> selectAll() {
        QueryWrapper<Breed> breedQueryWrapper = new QueryWrapper<>();
        List<Breed> breeds = breedMapper.selectList(breedQueryWrapper);
        return breeds!=null?Result.success(breeds):Result.error("-1","查找失败");
    }

    @Operation(summary = "分页查询品种信息")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String name,

                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Breed> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(name)){
            queryWrapper.eq(Breed::getBreedName,name);
        }
        Page<Breed> ResultPage = breedMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(ResultPage);
    }
}
