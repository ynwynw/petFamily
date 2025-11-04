package com.example.petback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Enum.RoomStatus;
import com.example.petback.common.Result;
import com.example.petback.entity.Animal;
import com.example.petback.entity.Room;
import com.example.petback.mapper.RoomMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name="宠物房间接口")
@RestController
@RequestMapping("/room")
public class RoomController {
    @Resource
    private RoomMapper roomMapper;

    @Operation(summary = "添加房间")
    @PostMapping
    public Result<?> add(@RequestBody Room room) {
        // 检查房间名称是否已存在
        if (roomMapper.selectCountByName(room.getName()) > 0) {
            return Result.error("-1","房间名称已存在，请使用其他名称。");
        }
        room.setStatus(RoomStatus.EMPTY.getInfo());
        int res = roomMapper.insert(room);
        return res>0?Result.success():Result.error("-1","添加失败");
    }

    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = roomMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }
    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = roomMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }
    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    public Result<?> updateById(@PathVariable int id,@RequestBody Room room) {
        int res = roomMapper.updateById(room);
        return res>0?Result.success():Result.error("-1","更新失败");
    }
    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public  Result<?> selectById(@PathVariable Integer id) {
        Room room = roomMapper.selectById(id);
        return room!=null?Result.success(room):Result.error("-1","查找失败");
    }
    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public   Result<?> selectAll() {
        QueryWrapper<Room> roomQueryWrapper = new QueryWrapper<>();
        List<Room> rooms = roomMapper.selectList(roomQueryWrapper);
        return rooms!=null?Result.success(rooms):Result.error("-1","查找失败");
    }
    @Operation(summary = "分页查询房间信息")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String name,

                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Room> queryWrapper = Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(name)){
            queryWrapper.eq(Room::getName,name);
        }
        Page<Room> ResultPage = roomMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        return Result.success(ResultPage);
    }

    @Operation(summary = "重置房间表ID计数器")
    @GetMapping("/resetCounter")
    public Result<?> resetCounter() {
        try {
            // 先清空表数据
            QueryWrapper<Room> queryWrapper = new QueryWrapper<>();
            roomMapper.delete(queryWrapper);
            
            // 重置自增计数器
            // 使用Mapper执行原生SQL语句重置自增计数器
            LambdaQueryWrapper<Room> wrapper = Wrappers.lambdaQuery();
            roomMapper.delete(wrapper);
            roomMapper.resetAutoIncrement();
            
            return Result.success("重置成功");
        } catch (Exception e) {
            return Result.error("-1", "重置失败：" + e.getMessage());
        }
    }

}
