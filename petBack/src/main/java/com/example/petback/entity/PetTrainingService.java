package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@TableName("pet_training_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetTrainingService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "服务的唯一标识符，自动递增")
    private Integer id;

    @Schema(description = "宠物")
    private String petName;

    @Schema(description = "训练服务的类型")
    private String serviceType;

    @Schema(description = "训练服务预计结束的日期，可以为空")
    private LocalDate endDate;


    @Schema(description = "训练的级别")
    private String trainingLevel;

    @Schema(description = "训练的目标或期望结果")
    private String trainingGoals;

    @Schema(description = "训练完成的状态，如“完成”、“进行中”、“未开始”等")
    private String completionStatus;
    @Schema(description = "用户姓名")
    private String userName;
}