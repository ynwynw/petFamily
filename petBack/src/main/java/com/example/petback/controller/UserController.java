package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.Result;
import com.example.petback.common.Enum.UserRole;
import com.example.petback.entity.Menu;
import com.example.petback.entity.User;
import com.example.petback.entity.UserPasswordUpdate;
import com.example.petback.mapper.MenuMapper;
import com.example.petback.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Tag(name="用户管理接口")
@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MenuMapper menuMapper;
    @Value("${user.defaultPassword}")
    private  String DEFAULT_PWD ;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    // 根据id获取用户信息
    @Operation(summary = "根据id获取用户信息")
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable int id) {
        LOGGER.info("GET user by ID: " + id);
        User user = userMapper.selectById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "根据username获取用户信息")
    @GetMapping("/getByUsername/{username}")
    public Result<?> getUserByUsername(@PathVariable String username) {
        User user_t = new User();
        user_t.setUsername(username);
        LOGGER.info("GET user by username: " + username);
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", user_t.getUsername()));
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<?> Login(@RequestBody User user) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUsername,user.getUsername());
        LOGGER.info("登录");
        User loginUser = userMapper.selectOne(queryWrapper);
        if(loginUser==null){
            return Result.error("-1","登录失败，用户不存在！");
        }
        UserRole loginRole = null;
        String role = loginUser.getRole();
        if(role.equals(UserRole.USER.getValue())){
            loginRole=UserRole.USER;
        }else if(role.equals(UserRole.ADMIN.getValue())){
            loginRole=UserRole.ADMIN;
        }else if(role.equals(UserRole.SUPER_ADMIN.getValue())){
            loginRole=UserRole.SUPER_ADMIN;
        }else{
            return Result.error("-1","登陆失败，角色信息异常");
        }
        if(loginRole.getId() != 3){

            // 查询角色为 roleId 或3 的菜单列表
            List<Menu> roleMenuList = menuMapper.selectList(Wrappers.<Menu>lambdaQuery()
                    .in(Menu::getRole, Arrays.asList(loginRole.getId(), 3)));
            //一级菜单
            List<Menu> parentList = roleMenuList.stream().filter(menu -> menu.getPid() == null).toList();
            for (Menu parentMenu : parentList) {
                List<Menu> childrenList = roleMenuList.stream().filter(menu -> parentMenu.getId().equals(menu.getPid())).collect(Collectors.toList());
                parentMenu.setChildren(childrenList);
            }
            loginUser.setMenuList(roleMenuList);
        }else {
            List<Menu> roleMenuList = menuMapper.selectList(Wrappers.<Menu>lambdaQuery());
            List<Menu> parentList = roleMenuList.stream().filter(menu -> menu.getPid() == null).toList();
            for (Menu parentMenu : parentList) {
                List<Menu> childrenList = roleMenuList.stream().filter(menu -> parentMenu.getId().equals(menu.getPid())).collect(Collectors.toList());
                parentMenu.setChildren(childrenList);
            }
            loginUser.setMenuList(roleMenuList);
        }

            if(user.getPassword().equals(loginUser.getPassword())){
                return Result.success(loginUser);
            }else{
                return Result.error("-1","登录失败，用户名或密码错误！");
            }

    }
    @Operation(summary = "密码修改")
    @PutMapping("/password/{id}")
    public Result<?> updatePassword(@PathVariable int id, @RequestBody UserPasswordUpdate userPasswordUpdate){
        User oldUser = userMapper.selectById(id);
        if(oldUser==null){
            return Result.error("-1","用户不存在");
        }else{
            if(userPasswordUpdate.getOldPassword().equals(oldUser.getPassword())){
                oldUser.setPassword(userPasswordUpdate.getNewPassword());
                int res = userMapper.updateById(oldUser);
                if(res>0){
                    return Result.success();
                }else{
                    return Result.error("-1","修改失败,请联系管理员");
                }
            }else{
                return Result.error("-1","旧密码错误,请重试！");
            }
        }
    }
    @Operation(summary = "重置密码")
    @GetMapping("/forget")
    public Result<?> forgetPassword(@RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "") String newPassword){
        User user = new User();
        if(StringUtils.isNotBlank(email)) {
            user.setEmail(email);
        }
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("email", user.getEmail()));
        if(userList==null|| userList.isEmpty()){
            return Result.error("-1","该邮箱不存在");
        }else{
            User oldUser =userList.get(0);
            if(StringUtils.isNotBlank(newPassword)) {
                oldUser.setPassword(newPassword);
                int res = userMapper.updateById(oldUser);
                if (res > 0) {
                    return Result.success();
                } else {
                    return Result.error("-1", "更新失败,请联系管理员");
                }
            }else{
                return Result.error("-1", "更新异常,请联系管理员");
            }

        }

    }

    // 分页查询用户
    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<?> getUsersByPage(
            @RequestParam(defaultValue = "") String username, // 查询条件，用户名
            @RequestParam(defaultValue = "")String sex,
            @RequestParam(defaultValue = "")String name,
            @RequestParam(defaultValue = "")String role,
            @RequestParam(defaultValue = "")String currentRole,
            @RequestParam(defaultValue = "1") Integer currentPage, // 当前页码
            @RequestParam(defaultValue = "10") Integer size // 每页条数
    ) {
        LOGGER.info("username:" + username + " sex:"+sex+" name"+name+"role"+role+ " currentRole:" +currentRole+" currentPage:" + currentPage + " size:" + size);
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        if(currentRole.equals(UserRole.ADMIN.getValue())){
            queryWrapper.eq(User::getRole,UserRole.USER.getValue());
        }else if(currentRole.equals(UserRole.SUPER_ADMIN.getValue())){
            queryWrapper.in(User::getRole, Arrays.asList(UserRole.USER.getValue(),UserRole.ADMIN.getValue()));
        }else{
            return Result.error("-1","身份认证失败");
        }
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(sex)) {
            queryWrapper.eq(User::getSex, sex);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(role)) {
            queryWrapper.eq(User::getRole, role);
        }


        Page<User> resultPage = userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        LOGGER.info("resultPageTotal: " + resultPage.getTotal());
        LOGGER.info("resultPage: " + resultPage.getRecords());
        LOGGER.info("resultPagePages: " + resultPage.getPages());
        return Result.success(resultPage);
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/role/{role}")
    public Result<?> getUserByRole(@PathVariable String role) {
        LOGGER.info("GET users by role: " + role);
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRole, role); // 假设User实体中有role字段
        List<User> users = userMapper.selectList(queryWrapper);
        LOGGER.info("GET users by role " + role + ": " + users);
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到该角色的用户");
        }
    }

    // 批量删除用户
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        LOGGER.info("DELETEBATCH user IDS:" + ids);
        int res = userMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    // 获取所有用户
    @Operation(summary = "获取所有用户")
    @GetMapping
    public Result<?> getAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        List<User> users = userMapper.selectList(queryWrapper);
        LOGGER.info("GET ALL users: " + users);
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }

    // 创建新用户
    @Operation(summary = "创建新用户")
    @PostMapping
    public Result<?> createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        Long checkUsername = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));


        if (checkUsername >0) {
            // 如果用户名已存在，返回错误信息
            return Result.error("-1", "注册失败，用户名已存在");
        }
        Long checkEmail = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));
        if (checkEmail>0) {
            // 如果邮箱已存在，返回错误信息
            return Result.error("-1", "注册失败，邮箱已存在");
        }
        if(!StringUtils.isNotBlank(user.getPassword())){
            user.setPassword(DEFAULT_PWD);
        }
        if(!StringUtils.isNotBlank(user.getRole())){
            user.setRole(UserRole.USER.getValue());
        }else{
            if (!Arrays.asList(UserRole.ADMIN.getValue(), UserRole.USER.getValue(), UserRole.SUPER_ADMIN.getValue()).contains(user.getRole())) {
                LOGGER.info("无效的角色: " + user.getRole());
                return Result.error("-1", "无效的角色");
            }
        }


        int res = userMapper.insert(user);
        if (res > 0) {
            return Result.success(user);
        } else {
            return Result.error("-1", "注册失败");
        }
    }

    // 更新用户信息
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable int id, @RequestBody User user) {
        LOGGER.info("PUT user ID: " + id + ", user: " + user);
        user.setId(id);
        int res = userMapper.updateById(user);
        if (res > 0) {
            return Result.success(user);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    // 用户余额充值接口
    @Operation(summary = "用户余额充值")
    @PutMapping("/recharge")
    public Result<?> rechargeUserBalance(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        Double amount = Double.parseDouble(params.get("amount").toString());
        
        LOGGER.info("用户充值 - 用户ID: " + userId + ", 金额: " + amount);
        
        if (userId == null || amount == null || amount <= 0) {
            return Result.error("-1", "参数错误或充值金额不正确");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        
        // 更新用户余额
        double oldBalance = user.getAccount() != null ? user.getAccount() : 0;
        double newBalance = oldBalance + amount;
        user.setAccount(newBalance);
        
        int res = userMapper.updateById(user);
        if (res > 0) {
            // 返回更新后的用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("newBalance", newBalance);
            result.put("addedAmount", amount);
            
            return Result.success(result);
        } else {
            return Result.error("-1", "充值失败，请稍后再试");
        }
    }

    // 根据id删除用户
    @Operation(summary = "根据id删除用户")
    @DeleteMapping("/{id}")
    public Result<?> deleteUserById(@PathVariable int id) {
        LOGGER.info("DELETE user ID: " + id);
        int res = userMapper.deleteById(id);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                .select(User::getId, User::getUsername, User::getName, User::getRole, 
                       User::getAvatar, User::getSex, User::getPhone, User::getEmail, 
                       User::getAddress, User::getAccount) // 返回除密码外的字段
                .like(StringUtils.isNotBlank(username), User::getUsername, username)
                .or()
                .like(StringUtils.isNotBlank(username), User::getName, username)
                .last("LIMIT " + limit); // 限制返回数量
                
        List<User> users = userMapper.selectList(wrapper);
        return Result.success(users);
    }
}