package com.example.petback.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.Result;
import com.example.petback.entity.Menu;
import com.example.petback.mapper.MenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name="菜单管理接口")
@RestController
@RequestMapping("/menu")
public class MenuController {
    @Resource
    MenuMapper menuMapper;

    public static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);
    private void updateMenuRole(Menu menu) {
        // 获取父级菜单
        Menu parentMenu = menuMapper.selectOne(Wrappers.<Menu>lambdaQuery().eq(Menu::getId, menu.getPid()));

        if (menu.getPid() == null) {
            // 当前menu是父级菜单，获取其所有子菜单
            List<Menu> submenus = menuMapper.selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getPid, menu.getId()));
            Integer parentRole = menu.getRole();

            // 遍历所有子菜单，更新它们的角色为父菜单的角色
            for (Menu submenu : submenus) {
                submenu.setRole(parentRole);
                menuMapper.updateById(submenu); // 更新子菜单角色
            }
            return;
        }
        if (parentMenu == null) {
            return; // 如果没有找到父级菜单，直接返回
        }

        Integer parentId = parentMenu.getId();
        Integer parentRole = parentMenu.getRole();
// 查询父级菜单下的所有子菜单
        List<Menu> childrenMenus = menuMapper.selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getPid, parentId));

        // 计数器，用于统计不同角色的子菜单数量
        int countRole0 = 0;
        int countRole1 = 0;
        int countRole2 = 0;
        int countRole3 = 0;

        // 遍历所有子菜单，统计角色
        for (Menu childMenu : childrenMenus) {
            Integer childRole = childMenu.getRole();
            if (childRole == 0) {
                countRole0++;
            } else if (childRole == 1) {
                countRole1++;
            } else if (childRole == 2) {
                countRole2++;
            } else if (childRole == 3) {
                countRole3++;
            }
        }

        // 根据子菜单的角色统计结果更新父级菜单的角色
        if (countRole0 == childrenMenus.size()) {
            // 所有子菜单角色都是0
            parentRole = 0;
        } else if (countRole1 == childrenMenus.size() || (countRole1 > 0 && countRole2 == 0 && countRole3 == 0)) {
            // 所有子菜单角色都是1或0
            parentRole = 1;
        } else if (countRole2 == childrenMenus.size() || (countRole2 > 0 && countRole1 == 0 && countRole3 == 0)) {
            // 所有子菜单角色都是2或0
            parentRole = 2;
        } else if (countRole1 > 0 && countRole2 > 0 || countRole3 > 0) {
            // 子菜单有1且有2，或包含3
            parentRole = 3;
        }

        // 更新父级菜单角色
        if (!parentRole.equals(parentMenu.getRole())) {
            parentMenu.setRole(parentRole);
            menuMapper.updateById(parentMenu);
        }

    }

    @Operation(summary = "增加菜单")
    @PostMapping("/save")
    public Result<?> save(@RequestBody Menu menu) {
        System.out.println(menu);

        Menu res = menuMapper.selectOne(Wrappers.<Menu>lambdaQuery().eq(Menu::getId, menu.getId()));
        if (res != null) {
            int i = menuMapper.updateById(menu);
            if(i>0){
                this.updateMenuRole(menu);
                return Result.success("更新成功");
            }else{
                return Result.error("-1","更新失败");
            }
        }else{
            int insert = menuMapper.insert(menu);
            if (insert>0){
                this.updateMenuRole(menu);//更新父级菜单
                return Result.success("插入成功");
            }else{
                return Result.error("-1","插入失败");
            }
        }

    }
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public Result<?>  update(@PathVariable int id, @RequestBody Menu menu) {
        menu.setId(id);
        LOGGER.info("UPDATE menu:"+menu);
        int res = menuMapper.updateById(menu);
        if(res>0){
            return Result.success();
        }else{
            return Result.error("-1","修改失败");
        }
    }
    @Operation(summary = "批量删除菜单")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int b = menuMapper.deleteBatchIds(ids);
        if(b>0){
            return Result.success();
        }else{
            return  Result.error("-1","删除失败");
        }

    }
    @Operation(summary = "根据id删除菜单项")
    @DeleteMapping("/deleteById/{id}")
    public Result<?> deleteById(@PathVariable("id") Integer id) {
        int result = menuMapper.deleteById(id);
        if (result == 0) {
            return Result.error("-1","删除失败，未找到指定的菜单项");
        }
        return Result.success("删除成功");
    }
    // 通过 ID 查询菜单项
    @GetMapping("/find/{id}")
    public Result<?> findById(@PathVariable("id") Integer id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return Result.error("-1","未找到指定的菜单项");
        }
        return Result.success(menu);
    }
@GetMapping("/findAll")
    public Result<?> findAll(@RequestParam(name = "name",defaultValue = "") String name){
    QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
    queryWrapper.orderByAsc("sort_num");
    if(StringUtils.isNotBlank(name)){
        queryWrapper.like("name",name);
    }
    List<Menu> allList = this.menuMapper.selectList(queryWrapper);
    //构造一级菜单
    List<Menu> parentList = allList.stream().filter(menu -> menu.getPid() == null).collect(Collectors.toList());
    for (Menu parentMenu : parentList) {
        List<Menu> childrenList = allList.stream().filter(menu -> parentMenu.getId().equals(menu.getPid())).collect(Collectors.toList());
        parentMenu.setChildren(childrenList);
    }
    System.out.println(parentList);
    return Result.success(parentList);

}
    @GetMapping("/findByPage")
    @Operation(summary = "分页查询菜单")
    public Result<?> findByPage(@RequestParam Integer currentPage,
                                @RequestParam Integer size,
                                @RequestParam(name = "name", defaultValue = "") String name) {
        Page<Menu> page = new Page<>(currentPage, size);
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_num");
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }

        Page<Menu> menuPage = menuMapper.selectPage(page, queryWrapper);

        // 构造一级菜单
        List<Menu> parentList = menuPage.getRecords().stream()
                .filter(menu -> menu.getPid() == null)
                .collect(Collectors.toList());

        // 设置子菜单
        for (Menu parentMenu : parentList) {
            List<Menu> childrenList = menuPage.getRecords().stream()
                    .filter(menu -> parentMenu.getId().equals(menu.getPid()))
                    .collect(Collectors.toList());
            parentMenu.setChildren(childrenList);
        }
        menuPage.setRecords(parentList);
        return Result.success(menuPage);
    }

}