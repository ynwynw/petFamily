package com.example.petback.controller;


import cn.hutool.json.JSONObject;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.petback.common.AliPayConfig;

import com.example.petback.common.Enum.OrderStatus;
import com.example.petback.entity.Goods;
import com.example.petback.entity.Orders;
import com.example.petback.mapper.GoodsMapper;
import com.example.petback.mapper.OrdersMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "阿里沙箱支付接口")
@RestController
@RequestMapping("/alipay")
public class
AliPayController {

    // 支付宝沙箱网关地址
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    //签名方式
    private static final String SIGN_TYPE = "RSA2";

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsMapper goodsMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AliPayController.class);
    @Operation(summary = "支付接口")
    @GetMapping("/pay")  //  /alipay/pay?orderNo=xxx
    public void pay(String orderNo, HttpServletResponse httpResponse) throws Exception {

        // 查询订单信息
        Orders order = ordersMapper.selectByOrderNo(orderNo);

        if (order == null) {

            return;
        }
        LOGGER.info("pay order:"+order);
        Integer goodsId = order.getGoodsId();
        Goods goods = goodsMapper.selectById(goodsId);
        LOGGER.info("pay goods:"+goods);
        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);
        LOGGER.info("alipay:"+aliPayConfig.toString());
        // 2. 创建 Request并设置Request参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        JSONObject bizContent = new JSONObject();

        // 订单的总金额
        BigDecimal orderNum = BigDecimal.valueOf(order.getNum()); // 将Integer转换为BigDecimal
        BigDecimal price = goods.getPrice(); // 假设这个方法返回BigDecimal
        BigDecimal totalAmount = orderNum.multiply(price); // 使用BigDecimal的multiply方

        bizContent.set("out_trade_no", order.getOrderNo());  // 我们自己生成的订单编号
        bizContent.set("total_amount", totalAmount); // 订单的总金额
        bizContent.set("subject", goods.getName());   // 支付的名称
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        LOGGER.info(bizContent.toString());
        request.setBizContent(bizContent.toString());

        request.setReturnUrl("http://localhost:8080/orders"); // 支付完成后自动跳转到本地页面的路径
        // 执行请求，拿到响应的结果，返回给浏览器
        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
    @Operation(summary = "支付回调接口")
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));


                String tradeNo = params.get("out_trade_no");
                String gmtPayment = params.get("gmt_payment");
                String alipayTradeNo = params.get("trade_no");
                // 更新订单状态为已支付，设置支付信息
                Orders orders = ordersMapper.selectByOrderNo(tradeNo);
                orders.setStatus(OrderStatus.W_Ship.name());

                Integer goodsId = orders.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                goods.setNum(goods.getNum()-orders.getNum());////更新货品库存
                goodsMapper.updateById(goods);
                ordersMapper.updateById(orders);
                LOGGER.info("order pay:"+ordersMapper);
            }
        }
    }

}