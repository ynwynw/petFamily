package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.SerializableString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "宠物上报表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("submit")
public class Submit implements Serializable {

    @TableId(value="id",type= IdType.AUTO)
    private Integer id;
    @Schema(description = "上报说明")
    private String name;
    @Schema(description = "上报时间")
    private String time;
    @Schema(description = "处理状态")
    private String status;
}
