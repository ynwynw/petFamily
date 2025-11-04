package com.example.petback.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.Service.HomeSliderService;
import com.example.petback.common.Result;
import com.example.petback.entity.HomeSlider;
import com.example.petback.entity.Submit;
import com.example.petback.mapper.HomeSliderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name="首页轮播图接口")
@RestController
@RequestMapping("/slider")
public class HomeSliderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeSliderController.class);
    @Resource
    private HomeSliderMapper homeSliderMapper;
    @Resource
    private HomeSliderService homeSliderService;

    @Operation(summary = "添加轮播图")
    @PostMapping
    public Result<?> add(@RequestParam("files") MultipartFile file, @RequestParam String homeSlider) {
        HomeSlider homeSliderEntity = JSON.parseObject(homeSlider, HomeSlider.class);

        String path = homeSliderService.updateImages(file, null);
        if(path!=null){
            homeSliderEntity.setImg(path);
        }
        int res = homeSliderMapper.insert(homeSliderEntity);
        return res > 0 ? Result.success() : Result.error("-1", "添加失败");
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        HomeSlider homeSlider = homeSliderMapper.selectById(id);
        int res = homeSliderMapper.deleteById(id);

        homeSliderService.updateImages(null,homeSlider.getImg());
        return res > 0 ? Result.success() : Result.error("-1", "删除失败");
    }


    @Operation(summary = "根据id更新")
    @PostMapping("/update/")
    public Result<?> updateById(@RequestParam("files") MultipartFile file,
                               @RequestParam(value = "deletedImg", required = false) String deleteImg,
                               @RequestParam String homeSlider) {
        try {
            HomeSlider homeSliderEntity = JSON.parseObject(homeSlider, HomeSlider.class);
            String path = null;
            
            // 只有当文件大小大于0时才更新图片
            if (file != null && file.getSize() > 0) {
                path = homeSliderService.updateImages(file, deleteImg);
                if (path != null) {
                    homeSliderEntity.setImg(path);
                }
            }
            
            int res = homeSliderMapper.updateById(homeSliderEntity);
            return res > 0 ? Result.success() : Result.error("-1", "更新失败");
        } catch (Exception e) {
            LOGGER.error("更新轮播图失败", e);
            return Result.error("-1", "更新失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        HomeSlider homeSlider = homeSliderMapper.selectById(id);
        return homeSlider != null ? Result.success(homeSlider) : Result.error("-1", "查找失败");
    }

    @Operation(summary = "查询所有轮播图")
    @GetMapping("/selectAll")
    public Result<?> selectAll() {
        List<HomeSlider> homeSliders = homeSliderMapper.selectList(null);
        return homeSliders != null ? Result.success(homeSliders) : Result.error("-1", "查找失败");
    }

    @Operation(summary = "分页查询轮播图信息")
    @GetMapping("/findAll")
    public Result<?> selectPage(
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<HomeSlider> queryWrapper = Wrappers.lambdaQuery();

        Page<HomeSlider> resultPage = homeSliderMapper.selectPage(new Page<>(page, limit), queryWrapper);
        LOGGER.info(String.valueOf(resultPage.getRecords()));
        return Result.success(resultPage);
    }
}