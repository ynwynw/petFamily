package com.example.petback.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petback.common.Enum.AdoptEnum;
import com.example.petback.common.Enum.FosterStatus;
import com.example.petback.common.Enum.OrderStatus;
import com.example.petback.common.Result;
import com.example.petback.entity.*;

import com.example.petback.mapper.*;
import com.example.petback.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "首页展示信息接口")
@RequestMapping("/show")
public class ShowController {
    @Autowired
    UserMapper userMapper;

    @Autowired
    FosterMapper fosterMapper;
    @Autowired
    AnimalMapper animalMapper;
    @Autowired
    BreedMapper breedMapper;
    @Autowired
    GoodsMapper goodsMapper;
    @Autowired
    AdoptMapper adoptMapper;
    @Autowired
    OrdersMapper ordersMapper;

    @Operation(summary = "获取首页卡片信息")
    @GetMapping("/getCardInfo")
    public Result<?> getCardInfo(){
        LocalDate lastMonthFirstDay = DateUtils.getLastMonthFirstDay();
        Long userCount = userMapper.selectCount(null)-1;
        Long animalCount = animalMapper.selectCount(null);
        Long fosterCount = fosterMapper.selectCount(null);
        Long breedCount = breedMapper.selectCount(null);
        Long goodsCount = goodsMapper.selectCount(null);

 ;

        ShowEntity showEntity = new ShowEntity(animalCount,userCount,breedCount,goodsCount,fosterCount);
        return Result.success(showEntity);




    }

    @GetMapping("/getStatistics")
    public Result<?> getStatistics() {
        try {
            // 获取基础统计数据
            Long animalCount = animalMapper.selectCount(null);
            Long userCount = userMapper.selectCount(null) - 1;
            Long adoptCount = adoptMapper.selectCount(null);
            Long orderCount = ordersMapper.selectCount(null);
            Long fosterCount = fosterMapper.selectCount(null);
            
            // 获取可领养宠物数量，使用枚举
            Long availableCount = animalMapper.selectCount(
                new QueryWrapper<Animal>().eq("status", AdoptEnum.NO_ADOPT.getInfo())
            );
            
            // 由于没有create_time字段，暂时将本月新增用户数设为0
            Long newUserCount = 0L;
            
            // 获取本月领养数量
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            Long monthAdoptCount = adoptMapper.selectCount(
                new QueryWrapper<Adopt>()
                    .ge("time", firstDayOfMonth.toString())
            );
            
            // 获取订单总金额，使用枚举值
            List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getStatus, OrderStatus.Completed.name())
            );

            BigDecimal totalAmount = BigDecimal.ZERO;
            if (orders != null) {
                for (Orders order : orders) {
                    if (order != null && order.getTotalAmount() != null) {
                        Goods goods = goodsMapper.selectById(order.getGoodsId());
                        totalAmount = totalAmount.add(goods.getPrice().multiply(BigDecimal.valueOf(order.getNum())));
                    }
                }
            }
            
            // 获取当前寄养数量，使用枚举
            Long currentFosterCount = fosterMapper.selectCount(
                new QueryWrapper<Foster>().eq("status", FosterStatus.Fostering.getInfo())
            );
            
            StatisticsEntity statistics = new StatisticsEntity(
                animalCount, availableCount, userCount, newUserCount,
                adoptCount, monthAdoptCount, orderCount, totalAmount,
                fosterCount, currentFosterCount
            );
            
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("-1", "获取统计数据失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/getAdoptTrend")
    public Result<?> getAdoptTrend() {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            
            List<Map<String, Object>> trendData = adoptMapper.selectAdoptTrend(
                startDate.toString(), endDate.toString()
            );
            
            return Result.success(trendData);
        } catch (Exception e) {
            return Result.error("-1","获取领养趋势数据失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/getAnimalStatus")
    public Result<?> getAnimalStatus() {
        try {
            List<Map<String, Object>> statusData = animalMapper.selectStatusDistribution();
            return Result.success(statusData);
        } catch (Exception e) {
            return Result.error("-1","获取宠物状态分布数据失败：" + e.getMessage());
        }
    }

    @GetMapping("/getAnimalBreed")
    public Result<?> getAnimalBreed() {
        try {
            // 获取所有品种
            List<Breed> breeds = breedMapper.selectList(null);
            List<Map<String, Object>> breedData = new ArrayList<>();
            
            // 统计每个品种的宠物数量
            for (Breed breed : breeds) {
                Long count = animalMapper.selectCount(
                    new QueryWrapper<Animal>().eq("breed", breed.getBreedName())
                );
                if (count > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("breed", breed.getBreedName());
                    item.put("count", count);
                    breedData.add(item);
                }
            }
            
            // 统计未分类的宠物数量
            Long otherCount = animalMapper.selectCount(
                new QueryWrapper<Animal>().isNull("breed").or().eq("breed", "")
            );
            if (otherCount > 0) {
                Map<String, Object> otherItem = new HashMap<>();
                otherItem.put("breed", "其他");
                otherItem.put("count", otherCount);
                breedData.add(otherItem);
            }
            
            return Result.success(breedData);
        } catch (Exception e) {
            return Result.error("-1", "获取宠物品种分布数据失败：" + e.getMessage());
        }
    }
}
