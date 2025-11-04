package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "房间表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    @Schema( description = "唯一标识符")
    private Integer id;
    @Schema(description = "房间名")
    private String name;
    @Schema(description = "房间状态")
    private String status;
//    可为空
@TableField(fill= FieldFill.UPDATE)
    @Schema(description = "宠物昵称")
    private String animal;
}
