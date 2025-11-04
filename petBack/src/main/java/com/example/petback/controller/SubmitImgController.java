package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petback.Service.SubmitImgService;
import com.example.petback.common.Result;
import com.example.petback.entity.SubmitImg;
import com.example.petback.mapper.SubmitImgMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@RestController
@Tag(name = "上报图片接口")
@RequestMapping("/submitImg")
public class SubmitImgController {
    @Resource
    private SubmitImgService submitImgService;
    @Tag(name = "获取图片")
    @GetMapping("/{submitId}")
    public Result<?> getUserBySubmitId(@PathVariable Integer submitId){
        List<SubmitImg> imgListBySubmitId = submitImgService.getImgListBySubmitId(submitId);
        return Result.success(imgListBySubmitId);
    }
//
//@PutMapping("/update/{submitId}")
//    public void updateImages(@RequestBody SubmitImgUpdateRequest submitImgUpdateRequest) {
//    List<String> imgList = submitImgUpdateRequest.getImgList();
//    Integer submitId = submitImgUpdateRequest.getSubmitId();
//
//    // 查询旧的图片数组
//        QueryWrapper<SubmitImg> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("submitId", submitId);
//        List<SubmitImg> oldImages = imageMapper.selectList(queryWrapper);
//
//        // 找出需要删除的图片
//        List<Integer> idsToDelete = oldImages.stream()
//                .filter(oldImage -> !imgList.contains(oldImage.getUrl()))
//                .map(SubmitImg::getId)
//                .collect(Collectors.toList());
//        // 删除旧图片
//        if (!idsToDelete.isEmpty()) {
//            imageMapper.deleteBatchIds(idsToDelete);
//        }
//
//        Set<String> oldImageUrls = oldImages.stream()
//                .map(SubmitImg::getUrl)
//                .collect(Collectors.toSet());
//
//// 使用 filter 方法找出新图片数组中不存在于旧图片数组中的 URL
//        List<SubmitImg> newImagesToInsert = imgList.stream()
//                .filter(url -> !oldImageUrls.contains(url))
//                .map(url -> {
//                    SubmitImg newImage = new SubmitImg();
//                    newImage.setSubmitId(submitId);
//                    newImage.setUrl(url);
//                    return newImage;
//                })
//                .collect(Collectors.toList());
//
//// 插入新图片到数据库
//        newImagesToInsert.forEach(imageMapper::insert);
//    }
}
