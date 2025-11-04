package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petback.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    @Select("SELECT COUNT(*) FROM room WHERE name = #{roomName}")
    int selectCountByName(@Param("roomName") String roomName);
    
    @Update("ALTER TABLE room AUTO_INCREMENT = 1")
    void resetAutoIncrement();
}
