package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petback.entity.Breed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface  BreedMapper extends BaseMapper<Breed> {
    @Select("SELECT COUNT(*) FROM breed WHERE breed_name = #{breedName}")
    int selectCountByName(@Param("breedName") String breedName);
}
