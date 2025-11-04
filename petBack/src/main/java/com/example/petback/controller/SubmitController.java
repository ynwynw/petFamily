package com.example.petback.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.Service.SubmitImgService;
import com.example.petback.common.Enum.SubmitEnum;
import com.example.petback.common.Result;
import com.example.petback.entity.Submit;
import com.example.petback.entity.SubmitImg;
import com.example.petback.mapper.SubmitImgMapper;
import com.example.petback.mapper.SubmitMapper;
import com.example.petback.utils.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/submit")
public class SubmitController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitController.class);


    @Resource
    private SubmitMapper submitMapper;
    @Resource
    private SubmitImgService submitImgService;
@Resource
private SubmitImgMapper submitImgMapper;


    @PostMapping
    public Result<?> add(@RequestParam("files[]") MultipartFile[] files,@RequestParam("submit") String submit) {
        LOGGER.info("submit add:"+submit);
        Submit submitEntity = JSON.parseObject(submit, Submit.class);
        submitEntity.setTime(DateUtil.now());
        submitEntity.setStatus(SubmitEnum.NO.getInfo());
        int res = submitMapper.insertReturnId(submitEntity);
        LOGGER.info("return id:"+submitEntity.getId());
        submitImgService.updateImages(files,null,submitEntity.getId());
        return res>0? Result.success():Result.error("-1","添加失败");
    }
    @PutMapping("/updateStatus/{id}")
    public Result<?> updateStatus(@PathVariable Integer id){
        Submit submit = submitMapper.selectById(id);

        submit.setStatus(SubmitEnum.YES.getInfo());
        boolean res=submitMapper.updateById(submit)>0;
        return res?Result.success("更新成功"):Result.error("-1","更新失败");

    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {

        int res = submitMapper.deleteById(id);
        QueryWrapper<SubmitImg> submitImgQueryWrapper = new QueryWrapper<>();
        submitImgQueryWrapper.eq("submit_id",id);
        List<SubmitImg> submitImgs = submitImgMapper.selectList(submitImgQueryWrapper);

        List<String> delImgs = new ArrayList<>();
        for (SubmitImg submitImg : submitImgs) {
            delImgs.add(submitImg.getUrl());
        }

        submitImgService.updateImages(null,delImgs,id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = submitMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }

    @Operation(summary = "根据id更新")
    @PostMapping("/update")
    public Result<?> upload(@RequestParam("files[]") MultipartFile[] files,@RequestParam("submit") String submit,@RequestParam("deletedImgs") String deleteImgs) {
        Submit submitEntity = JSON.parseObject(submit, Submit.class);
        List<String> deleteUrls = JSON.parseObject(deleteImgs, new TypeReference<List<String>>() {});
        ArrayList<String> imgList = new ArrayList<>();
        submitMapper.updateById(submitEntity);
        submitImgService.updateImages(files,deleteUrls,submitEntity.getId());
        return Result.success();
    }


    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        Submit submit = submitMapper.selectById(id);
        return submit!=null?Result.success(submit):Result.error("-1","查找失败");
    }

    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public Result<?> selectAll( ) {
        QueryWrapper<Submit> queryWrapper = new QueryWrapper<>();
        List<Submit> submits = submitMapper.selectList(queryWrapper);

        return submits!=null?Result.success(submits):Result.error("-1","查找失败");
    }

    @Operation(summary = "分页查询上报信息")
    @GetMapping("/page")
    public Result<?> getUsersByPage(
            @RequestParam(defaultValue = "") String name, // 查询条件，用户名


            @RequestParam(defaultValue = "1") Integer currentPage, // 当前页码
            @RequestParam(defaultValue = "10") Integer size // 每页条数
    ) {

        LambdaQueryWrapper<Submit> queryWrapper = Wrappers.lambdaQuery();

        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(Submit::getName, name);
        }

        Page<Submit> resultPage = submitMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        LOGGER.info("resultPageTotal: " + resultPage.getTotal());
        LOGGER.info("resultPage: " + resultPage.getRecords());
        LOGGER.info("resultPagePages: " + resultPage.getPages());
        return Result.success(resultPage);
    }

}
