package com.example.petback.controller.email;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.petback.common.Result;
import com.example.petback.entity.User;
import com.example.petback.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@RequestMapping("/email")
public class SendEmailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailController.class);

    @Resource
    JavaMailSender javaMailSender;
    @Value("${user.fromEmail}")
    private String FROM_EMAIL;
    @Resource
    UserMapper userMapper;
    @PostMapping("/sendEmail")
    public Result<?> emailRegister(@RequestBody User user){
    Long email = userMapper.selectCount(new QueryWrapper<User>().eq("email", user.getEmail()));
    if(email>0){
        Result.error("-1","邮箱已存在，请勿重复注册");
    }
    Random random = new Random();
   int code= random.nextInt(899999)+100000;
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setFrom(FROM_EMAIL);
    simpleMailMessage.setTo(user.getEmail());
    simpleMailMessage.setSubject("管理系统验证码");
    simpleMailMessage.setText("邮箱验证码为："+code+",请勿转发给他人");
    try{

    javaMailSender.send(simpleMailMessage);
    LOGGER.info("邮件已发送："+simpleMailMessage.getText());
    return Result.success(code);
    }catch (Exception e){
        LOGGER.error("邮件发送异常。"+e.getMessage());
        return Result.error("-1","验证码发送异常，请联系管理员。");
    }

}
    @GetMapping("/findByEmail")
    public Result<?> findByEmail(@RequestParam String email) {
        LOGGER.info("FIND BY EMAIL:"+email);
        User userWithEmail = new User();
        userWithEmail.setEmail(email);
        // 检查邮箱是否存在
        User emailUser = userMapper.selectOne(new QueryWrapper<User>().eq("email", userWithEmail.getEmail()));
        if (emailUser == null) {
            return Result.error("-1", "邮箱不存在，请检查邮箱地址是否正确");
        }

        // 生成随机验证码
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;

        // 构建邮件消息
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(FROM_EMAIL);
        simpleMailMessage.setTo(userWithEmail.getEmail());
        simpleMailMessage.setSubject("找回密码验证码");
        simpleMailMessage.setText("您的找回密码验证码为：" + code + "，有效期为5分钟，请勿泄露给他人。");

        try {
            // 发送邮件
            javaMailSender.send(simpleMailMessage);
            LOGGER.info("找回密码邮件已发送：" + simpleMailMessage.getText());
            return Result.success(code);
        } catch (Exception e) {
            LOGGER.error("找回密码邮件发送异常：" + e.getMessage());
            return Result.error("-1", "邮件发送异常，请联系管理员。");
        }
    }

}
