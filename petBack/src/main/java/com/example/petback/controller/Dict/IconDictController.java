package com.example.petback.controller.Dict;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.Result;

import com.example.petback.mapper.Dict.IconDictMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.petback.common.Result.error;
import static com.example.petback.common.Result.success;


@RestController
@RequestMapping("/icon-dict")
public class IconDictController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IconDictController.class);
    @Autowired
    private IconDictMapper dictItemMapper;

    /**
     * 新增/修改字典项
     * @param iconDict
     * @return
     */
    @PostMapping("/save")
    @Operation(summary = "新增/修改字典项")
    public Result<?> save(@RequestBody @Valid com.example.petback.entity.Dict.IconDict iconDict) {
        QueryWrapper<com.example.petback.entity.Dict.IconDict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("item_key", iconDict.getItemKey());
        List<Object> exitDictItem = dictItemMapper.selectObjs(queryWrapper);
        if(exitDictItem!=null){
            return error("-1","该项目已存在");
        }
        Boolean result = dictItemMapper.insert(iconDict)>0;

        return result? success() : error("-1", "保存失败");
    }


    @Operation(summary = "更新字典项")
    @PutMapping("/{id}")
    public Result<?>  update(@PathVariable int id, @RequestBody com.example.petback.entity.Dict.IconDict iconDict) {
        iconDict.setId(id);

        int res = dictItemMapper.updateById(iconDict);
        if(res>0){
            return success(iconDict);
        }else{
            return error("-1","修改失败");
        }
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有")
    public Result<?> getAll() {

        com.example.petback.entity.Dict.IconDict iconDict = new com.example.petback.entity.Dict.IconDict();

        QueryWrapper<com.example.petback.entity.Dict.IconDict> queryWrapper = new QueryWrapper<>();
        List<com.example.petback.entity.Dict.IconDict> itemList = dictItemMapper.selectList(queryWrapper);

        return success(itemList);
    }
    /**
     * 根据ID删除字典项
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    @Operation(summary = "根据ID删除字典项")
    public Result<?> deleteById(@PathVariable Integer id) {
        boolean result = dictItemMapper.deleteById(id) > 0;
        return result ? success() : error("-1", "删除失败");
    }

    /**
     * 批量删除字典项
     * @param idList
     * @return
     */
    @PostMapping("/deleteBatch")
    @Operation(summary = "批量删除字典项")
    public Result<?> deleteBatch(@RequestBody List<Integer> idList) {
        boolean result = dictItemMapper.deleteBatchIds(idList) > 0;
        return result ? success() : error("-1", "删除失败");
    }



    /**
     * 分页查询字典项
     * @param currentPage
     * @param size
     * @param itemKey
     * @return
     */
    @GetMapping("/findPage")
    @Operation(summary = "分页查询字典项")
    public Result<?> findPage(@RequestParam Integer currentPage,
                              @RequestParam Integer size,

                              @RequestParam(name = "itemKey", defaultValue = "") String itemKey) {
        Page<com.example.petback.entity.Dict.IconDict> page = new Page<>(currentPage, size);
        com.example.petback.entity.Dict.IconDict iconDict = new com.example.petback.entity.Dict.IconDict();

        iconDict.setItemKey(itemKey);
        QueryWrapper<com.example.petback.entity.Dict.IconDict> queryWrapper = new QueryWrapper<>();

        if (!itemKey.isEmpty()) {
            queryWrapper.like("item_key", iconDict.getItemKey());
        }
        Page<com.example.petback.entity.Dict.IconDict> dictItemPage = dictItemMapper.selectPage(page, queryWrapper);
        return success(dictItemPage);
    }
}