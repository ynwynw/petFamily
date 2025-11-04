package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.entity.Adopt;
import com.example.petback.entity.Foster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FosterMapper extends BaseMapper<Foster> {
    List<Foster> selectAll();

    Page<Foster> selectByPage(@Param("status")String status , @Param("animalName") String animalName, @Param("userId") Integer userId, Page<Foster> page
    );
}
