package com.example.petback.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petback.entity.SubmitImg;
import com.example.petback.mapper.HomeSliderMapper;
import com.example.petback.utils.FileUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class HomeSliderService {


        private static final Logger LOGGER = LoggerFactory.getLogger(com.example.petback.Service.HomeSliderService.class);
        @Resource
        private HomeSliderMapper homeSliderMapper;
    @Transactional
    public String updateImages(MultipartFile  newFile, String oldImg) {
        LOGGER.info("imgList:" + oldImg);

    String newPath=null;
        if(oldImg!=null){
            String newUrl = FileUtil.removePartFromUrlUsingJSON(oldImg, "/api/img/");
            LOGGER.info("delete file:"+newUrl);
            FileUtil.deleteFile(newUrl);

        }
        if(newFile!=null){
            newPath = FileUtil.saveImage(newFile,"homeSlider");

        }
        return newPath;



    }

}
