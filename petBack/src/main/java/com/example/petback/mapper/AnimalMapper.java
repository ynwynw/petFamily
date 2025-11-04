package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petback.entity.Animal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnimalMapper extends BaseMapper<Animal> {
    
    @Select("SELECT status, COUNT(*) as count " +
            "FROM animal " +
            "GROUP BY status")
    List<Map<String, Object>> selectStatusDistribution();
}
