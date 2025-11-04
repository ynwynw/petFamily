package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.entity.Foster;
import com.example.petback.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
    Page<HealthRecord> selectByPage(@Param("petName")String petName , @Param("vaccinationDate") String vaccinationDate, Page<HealthRecord> page);


}
