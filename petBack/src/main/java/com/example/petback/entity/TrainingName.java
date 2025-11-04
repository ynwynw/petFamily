package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@TableName("training_names")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "训练ID")
    private Integer id;

    @Schema(description = "训练名称")
    private String name;
    @Schema(description = "训练描述")
    private String description;
}