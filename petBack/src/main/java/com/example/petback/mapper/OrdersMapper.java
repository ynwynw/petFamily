package com.example.petback.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.entity.OrdersAmount;
import com.example.petback.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    Page<Orders> selectOrdersByPage(Page<Orders> page,
                                   @Param("orderNo") String orderNo,
                                   @Param("goodsId") Integer goodsId,
                                   @Param("userId") Integer userId,
                                   @Param("goodsName") String goodsName,
                                   @Param("userName") String userName);
    List<Orders> selectOrdersWithOrderAndUsername(
                                                 Integer userId
                                          );
    OrdersAmount selectTotalOrderAmount();
    OrdersAmount selectOrderAmountForLastMonth(Date lastMonthDate);

    Orders selectOrdersById(Integer id);

    Orders selectByOrderNo(String orderNo);

    @Select("SELECT COALESCE(SUM(total_amount), 0) as total " +
            "FROM orders " +
            "WHERE status = #{status}")
    BigDecimal selectTotalAmount(@Param("status") String status);
}
