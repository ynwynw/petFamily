package com.example.petback.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.common.Enum.FosterStatus;
import com.example.petback.common.Enum.RoomStatus;
import com.example.petback.common.Result;
import com.example.petback.entity.Adopt;
import com.example.petback.entity.Foster;
import com.example.petback.entity.Room;
import com.example.petback.mapper.FosterMapper;
import com.example.petback.mapper.RoomMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foster")
public class FosterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FosterController.class);
    @Resource
    private FosterMapper fosterMapper;
    @Resource
    private RoomMapper roomMapper;


    @Operation(summary = "添加记录")
    @PostMapping
    public Result<?> add(@RequestBody Foster foster) {
        foster.setStatus(FosterStatus.W_HANDLE.getInfo());
        int res = fosterMapper.insert(foster);
        return res>0?Result.success():Result.error("-1","添加失败");
    }
    @Operation(summary = "根据id删除")
    @DeleteMapping("/{id}")
    public Result<?> deleteById(@PathVariable Integer id) {
        int res = fosterMapper.deleteById(id);
        return res>0?Result.success():Result.error("-1","删除失败");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        boolean flag =true;
        for (Integer id : ids) {
            flag = fosterMapper.deleteById(id)>0;
            if(!flag) break;
        }
        return flag?Result.success():Result.error("-1","（部分）删除失败");
    }

    @Operation(summary = "根据id更新")
    @PutMapping("/{id}")
    @Transactional
    public Result<?> updateById(@PathVariable Integer id,@RequestBody Foster foster) {
        boolean flag=true;
        LOGGER.info("UPDATE FOLSTER:"+foster);
        Foster old = fosterMapper.selectById(id);
        flag = fosterMapper.updateById(foster)>0;
        if(foster.getStatus().equals(FosterStatus.END.getInfo())){
            Room room = roomMapper.selectById(foster.getRoomId());
            if (room != null) {
                room.setStatus(RoomStatus.EMPTY.getInfo());
                room.setAnimal(null);
                flag = roomMapper.updateById(room)>0;

            }

            return flag?Result.success():Result.error("-1","更新失败");
        }
        // 对应的房间状态要修改成占用，房间对应的在养宠物也要更新

        Room room = roomMapper.selectById(foster.getRoomId());
        if (old.getRoomId()==null||!old.getRoomId().equals(foster.getRoomId())) {
            LOGGER.info("ROOM "+room);
            if(room!=null){
                if(!room.getStatus().equals(RoomStatus.EMPTY.getInfo())){
                    return Result.error("-1","该房间已被占用！");
                }
                room.setStatus(RoomStatus.USING.getInfo());
                room.setAnimal(foster.getName());
                flag = roomMapper.updateById(room)>0;
            }

        }
        return flag?Result.success():Result.error("-1","更新失败");
    }




    @Operation(summary = "根据id查询")
    @GetMapping("/selectById/{id}")
    public Result<?> selectById(@PathVariable Integer id) {
        Foster foster = fosterMapper.selectById(id);
        return foster!=null?Result.success(foster):Result.error("-1","查找失败");
    }

    @Operation(summary = "查询所有")
    @GetMapping("/selectAll")
    public Result<?> selectAll( ) {
        List<Foster> fosters = fosterMapper.selectAll();
        return fosters!=null?Result.success(fosters):Result.error("-1","查找失败");
    }



    @Operation(summary = "分页查询寄养记录")
    @GetMapping("/selectPage")
    public Result<?> selectPage(@RequestParam(defaultValue = "") String animalName,
                                @RequestParam(defaultValue = "") Integer userId,
                                @RequestParam(defaultValue = "") String status,
                                @RequestParam(defaultValue = "1") Integer currentPage,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Foster> page = new Page<>(currentPage, size);
        Page<Foster> fosterPage = fosterMapper.selectByPage(status,animalName,userId,page);
        LOGGER.info("SELECT PAGE:"+fosterPage.getRecords());
        return Result.success(fosterPage);
    }
}



