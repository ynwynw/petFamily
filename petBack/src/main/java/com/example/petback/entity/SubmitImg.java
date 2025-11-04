package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "宠物信息上报图片")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("submit_img")
public class SubmitImg {
    @TableId(value="id",type= IdType.AUTO)
    @Schema(description = "图片id")
    Integer id;
    @Schema(description = "上报id")
    Integer submitId;
    @Schema(description = "图片路径")
    String url;
}
