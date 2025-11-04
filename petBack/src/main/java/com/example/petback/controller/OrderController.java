package com.example.petback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.petback.common.AliPayConfig;

import com.example.petback.common.Enum.OrderStatus;
import com.example.petback.common.Result;
import com.example.petback.common.Enum.UserRole;
import com.example.petback.entity.Goods;
import com.example.petback.entity.Orders;
import com.example.petback.entity.User;
import com.example.petback.mapper.GoodsMapper;
import com.example.petback.mapper.OrdersMapper;
import com.example.petback.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "订单接口")
@RequestMapping("/orders")
@RestController
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserMapper userMapper;


    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";

    @Resource
    private AliPayConfig aliPayConfig;

    // 根据id获取订单详情
    @Operation(summary = "根据id获取订单")
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable int id) {
        LOGGER.info("GET order by ID: " + id);
        if(id>0) {
            Orders order = ordersMapper.selectOrdersById(id);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("-1", "未找到订单");
            }
        }else{
            return Result.error("-1", "未找到订单");
        }

    }

    // 分页查询订单
    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<?> getOrdersByPage(
            @RequestParam(defaultValue = "") String orderNo,
            @RequestParam(defaultValue = "") Integer goodsId,
            @RequestParam(defaultValue = "") Integer userId,
            @RequestParam(defaultValue="")String userRole,
            @RequestParam(defaultValue = "") String goodsName,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("orderNo: " + orderNo + ", currentPage: " + currentPage + ", size: " + size);
        Integer searchUserId = userId;
        Page<Orders> page = new Page<>(currentPage, size);
        if(!userRole.equals(UserRole.USER.toString())){
            searchUserId=0;
        }
        Page<Orders> resultPage = ordersMapper.selectOrdersByPage(page, orderNo, goodsId, searchUserId, goodsName, username);
        LOGGER.info("resultPageTotal: " + resultPage.getTotal());
        LOGGER.info("resultPage: " + resultPage.getRecords());
        return Result.success(resultPage);
    }

    // 批量删除订单
    @Operation(summary = "批量删除订单")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        LOGGER.info("DELETE BATCH order IDS: " + ids);
        int res = ordersMapper.deleteBatchIds(ids);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    // 创建新的订单
    @Operation(summary = "创建新的订单")
    @PostMapping

    public Result<?> createOrder(@RequestBody Orders order) {
        LOGGER.info("ORDER CREATE:"+order);
        // 生成唯一的订单号
        Integer goodsId = order.getGoodsId();
        Integer userId = order.getUserId();

        User user = userMapper.selectById(userId);
        Goods goods = goodsMapper.selectById(goodsId);
        if(goods==null){
            return Result.error("-1","货品不存在");
        }else{
            if(goods.getNum()<order.getNum()){
                return Result.error("-1","库存不足");
            }
        }
        if(user==null){
            return Result.error("-1","找不到该用户！");
        }
        String orderNo = String.valueOf(System.currentTimeMillis());
        order.setOrderNo(orderNo);
        order.setGoodsName(goods.getName());

        // 设置订单状态默认为 "W_Pay"
        order.setStatus(OrderStatus.W_Pay.toString());
        LOGGER.info("POST order: " + order);
        int res = ordersMapper.insert(order);
        if (res > 0) {
            // 返回创建成功的订单信息
            return Result.success(order);
        } else {
            // 返回创建失败的错误信息
            return Result.error("-1", "创建订单失败");
        }

    }

    // 更新订单信息
    @Operation(summary = "更新订单信息")
    @PutMapping("/{id}")
    public Result<?> updateOrder(@PathVariable int id, @RequestBody Orders order) {
        Orders newOrders = ordersMapper.selectById(id);
        String status = order.getStatus();
        if (!(status.equals(OrderStatus.W_Pay.toString()) ||
                status.equals(OrderStatus.W_Ship.toString()) ||
                status.equals(OrderStatus.W_Pickup.toString()) ||
                status.equals(OrderStatus.Completed.toString()))) {
            return Result.error("-1", "订单状态错误："+status);
        }
        
        if(newOrders == null) {
            return Result.error("-1", "订单不存在");
        }
        
        newOrders.setStatus(order.getStatus());
        LOGGER.info("PUT order ID: " + id + ", order: " + order);
        int res = ordersMapper.updateById(newOrders);
        if (res > 0) {
            return Result.success(order);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    // 根据id删除订单
    @Operation(summary = "根据id删除订单")
    @DeleteMapping("/{id}")
    public Result<?> deleteOrderById(@PathVariable int id) {
        LOGGER.info("DELETE order ID: " + id);
        int res = ordersMapper.deleteById(id);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    // 根据订单号获取订单详情
    @Operation(summary = "根据订单号获取订单")
    @GetMapping("/getByOrderNo/{orderNo}")
    public Result<?> getOrderByOrderNo(@PathVariable String orderNo) {
        LOGGER.info("GET order by OrderNo: " + orderNo);
        if(orderNo != null && !orderNo.isEmpty()) {
            Orders order = ordersMapper.selectByOrderNo(orderNo);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("-1", "未找到订单");
            }
        } else {
            return Result.error("-1", "订单号为空");
        }
    }
    
    // 余额支付接口
    @Operation(summary = "余额支付")
    @PostMapping("/balancePay")
    public Result<?> balancePay(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        Integer userId = (Integer) params.get("userId");
        
        LOGGER.info("Balance pay - orderNo: " + orderNo + ", userId: " + userId);
        
        if(orderNo == null || userId == null) {
            return Result.error("-1", "参数错误");
        }
        
        // 1. 获取订单信息
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if(order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 2. 检查订单状态是否为待支付
        if(!order.getStatus().equals(OrderStatus.W_Pay.toString())) {
            return Result.error("-1", "订单状态错误，只能支付待支付状态的订单");
        }
        
        // 3. 获取用户信息
        User user = userMapper.selectById(userId);
        if(user == null) {
            return Result.error("-1", "用户不存在");
        }
        
        // 4. 获取商品信息计算订单金额
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if(goods == null) {
            return Result.error("-1", "商品不存在");
        }
        
        // 计算订单总金额
        BigDecimal totalAmount = goods.getPrice().multiply(new BigDecimal(order.getNum()));
        
        // 5. 检查用户余额是否足够
        if(user.getAccount() < totalAmount.doubleValue()) {
            return Result.error("-1", "余额不足");
        }
        
        // 6. 扣减用户余额
        user.setAccount(user.getAccount() - totalAmount.doubleValue());
        int userUpdateResult = userMapper.updateById(user);
        
        if(userUpdateResult <= 0) {
            return Result.error("-1", "扣减余额失败");
        }
        
        // 7. 修改订单状态为待发货
        order.setStatus(OrderStatus.W_Ship.toString());
        int orderUpdateResult = ordersMapper.updateById(order);
        
        if(orderUpdateResult <= 0) {
            // 回滚用户余额
            user.setAccount(user.getAccount() + totalAmount.doubleValue());
            userMapper.updateById(user);
            return Result.error("-1", "更新订单状态失败");
        }
        
        return Result.success();
    }
    
    // 用户确认自提接口
    @Operation(summary = "用户确认自提")
    @PostMapping("/confirmPickup")
    public Result<?> confirmPickup(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        Integer userId = (Integer) params.get("userId");
        
        LOGGER.info("Confirm pickup - orderNo: " + orderNo + ", userId: " + userId);
        
        if(orderNo == null || userId == null) {
            return Result.error("-1", "参数错误");
        }
        
        // 1. 获取订单信息
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if(order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 2. 检查订单是否属于当前用户
        if(!order.getUserId().equals(userId)) {
            return Result.error("-1", "无法确认非自己的订单");
        }
        
        // 3. 检查订单状态是否为待自提
        if(!order.getStatus().equals(OrderStatus.W_Pickup.toString())) {
            return Result.error("-1", "订单状态错误，只能确认待自提状态的订单");
        }
        
        // 4. 修改订单状态为已完成
        order.setStatus(OrderStatus.Completed.toString());
        int orderUpdateResult = ordersMapper.updateById(order);
        
        if(orderUpdateResult <= 0) {
            return Result.error("-1", "确认自提失败");
        }
        
        return Result.success();
    }

}