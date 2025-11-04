package com.example.petback.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "首页信息展示实体")

@NoArgsConstructor
@AllArgsConstructor


public class ShowEntity {
    @Schema(description = "宠物数量")
    private Long animalCount;
    @Schema(description = "用户总数")
    private Long userCount;
    @Schema(description = "宠物品种")
    private Long kindCount;
    @Schema(description = "宠物用品统计")
    private Long goodsCount;
    @Schema(description = "寄养总数")
    private Long fosterNum;

}
