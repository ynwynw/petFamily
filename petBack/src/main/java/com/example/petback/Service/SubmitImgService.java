package com.example.petback.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petback.entity.Submit;
import com.example.petback.entity.SubmitImg;
import com.example.petback.mapper.SubmitImgMapper;
import com.example.petback.utils.FileUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubmitImgService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitImgService.class);
    @Resource
    private SubmitImgMapper imageMapper;
    @Transactional
    public void updateImages(MultipartFile[]  newFiles, List<String> oldImgs, Integer submitId) {
        LOGGER.info("imgList:" + oldImgs);


        if(oldImgs!=null){
            for (String oldImg : oldImgs) {
                QueryWrapper<SubmitImg> queryWrapper = new QueryWrapper<>();
                String newUrl = FileUtil.removePartFromUrlUsingJSON(oldImg, "/api/img/");
                LOGGER.info("delete file:"+newUrl);
                queryWrapper.eq("url", newUrl);
                imageMapper.delete(queryWrapper);
                FileUtil.deleteFile(newUrl);
            }
        }
        if(newFiles!=null){
            for (MultipartFile newFile : newFiles) {
                String path = FileUtil.saveImage(newFile,"submit");
                SubmitImg submitImg = new SubmitImg(0, submitId, path);
                LOGGER.info("add file:"+submitImg.getUrl());
                imageMapper.insert(submitImg);
            }

        }


    }

    public List<SubmitImg> getImgListBySubmitId(Integer submitId){
        QueryWrapper<SubmitImg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("submit_id", submitId);
        List<SubmitImg> imgs = imageMapper.selectList(queryWrapper);
        ArrayList<String> urls = new ArrayList<>();
//        for (SubmitImg img : imgs) {
//            urls.add(img.getUrl());
//        }
        return imgs;
    }

}
