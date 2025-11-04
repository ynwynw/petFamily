package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.entity.PetNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetNotificationMapper extends BaseMapper<PetNotification> {
    Page<PetNotification> selectByPage(
            @Param("petName") String petName,
            @Param("status") String status,
            @Param("page") Page<PetNotification> page
    );
} 