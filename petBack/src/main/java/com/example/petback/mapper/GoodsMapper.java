package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petback.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
