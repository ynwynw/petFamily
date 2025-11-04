package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Result;
import com.example.petback.entity.Foster;
import com.example.petback.entity.Goods;
import com.example.petback.entity.User;
import com.example.petback.mapper.GoodsMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsController.class);
    @Resource
    private GoodsMapper goodsMapper;

    @Operation(summary = "添加记录")
    @PostMapping
    public Result<?> add(@RequestBody Goods goods)  {
        int res = goodsMapper.insert(goods);
        return res>0? Result.success():Result.error("-1","添加失败");
    }
    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = goodsMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = goodsMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }

    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    @Transactional
    public Result<?> updateById(@PathVariable Integer id,@RequestBody Goods goods) {
        boolean flag = goodsMapper.updateById(goods)>0;
        return flag?Result.success():Result.error("-1","更新失败");
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        Goods goods = goodsMapper.selectById(id);
        return goods!=null?Result.success(goods):Result.error("-1","查找失败");
    }

    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public Result<?> selectAll( ) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        List<Goods> goodsList = goodsMapper.selectList(queryWrapper);
        return goodsList!=null?Result.success(goodsList):Result.error("-1","查找失败");
    }

    @Operation(summary = "获取所有产品")
    @GetMapping
    public Result<?> getAllGoods( @RequestParam(defaultValue = "") String name) {
        LambdaQueryWrapper<Goods> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(name)){
            queryWrapper.like(Goods::getName,name);
        }
        List<Goods> goodsList = goodsMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL goods: " + goodsList);
        return Result.success(goodsList);
    }
    // 分页查询农产品
    @Operation(summary = "分页查询产品")
    @GetMapping("/page")
    public Result<?> getGoodsByPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("name: " + name + ", currentPage: " + currentPage + ", size: " + size);
        Page<Goods> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<Goods> queryWrapper = Wrappers.lambdaQuery();

        if(StringUtils.isNotBlank(name)){
            queryWrapper.like(Goods::getName, name);
        }
        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        return Result.success(goodsPage);
    }
    
}
