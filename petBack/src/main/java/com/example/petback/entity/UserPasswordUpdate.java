package com.example.petback.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "更新密码密码实体类")
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordUpdate {
    @Schema(description = "旧密码")
    private String oldPassword; // 旧密码
    @Schema(description = "新密码")
    private String newPassword; // 新密码
}
