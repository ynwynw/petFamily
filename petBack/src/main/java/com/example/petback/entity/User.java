package com.example.petback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;

@Schema(description = "用户信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    @Schema(description = "自增主键")
    private Integer id;
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "角色", allowableValues = "USER,ADMIN,SUPER_ADMIN")
    private String role;
    @Schema(description = "性别")
    private String sex;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "邮箱")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确")
    private String email;
    @Schema(description = "地址")
    private String address;
    @TableField(exist = false)
    private List<Menu> menuList;

 @Schema(description = "余额")
    private Double account;


}
