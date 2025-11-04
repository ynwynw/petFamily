package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petback.entity.Submit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface SubmitMapper extends BaseMapper<Submit> {
//    @Insert("insert into submit (name,time,status) values (#{name},#{time},#{status})")
//    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertReturnId(Submit submit);
}
