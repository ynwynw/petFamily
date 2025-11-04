package com.example.petback.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import com.example.petback.common.Result;
import com.example.petback.utils.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传接口类")
@RequestMapping("/file")
@RestController
public class FileController {
    private final String FILE_BASE_PATH = System.getProperty("user.dir") + "/files/";

    // 定义静态资源目录的相对路径
    private static final String STATIC_FILES_DIR = "/static/files/";
    private static final String FILE_UPLOAD_DIR = "files/";

    @Value("${server.servlet.context-path}")
    private String contextPath; // 应用的上下文路径
    @Value("${baseUrl}")
    private String fileBaseUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result<?> upLoad(@RequestParam("file") MultipartFile file) {
                if (StringUtils.isEmpty(file.getOriginalFilename())) {
                    LOGGER.error("文件不存在");
                    return Result.error("-1","文件不存在！");
                }
                LOGGER.info("upload FILE:"+file.getOriginalFilename());
        String path = FileUtil.saveImage(file,null);
        if(StringUtils.isNotBlank(path)){
            return Result.success(path);
        }else{
            return Result.error("-1","文件上传失败");
        }


    }

}





