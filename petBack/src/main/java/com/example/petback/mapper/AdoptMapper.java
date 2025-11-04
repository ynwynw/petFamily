package com.example.petback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.petback.entity.Adopt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
@Mapper
public interface AdoptMapper extends BaseMapper<Adopt> {
    List<Adopt> selectAll(Adopt adopt);
    Page<Adopt> selectByPage(@Param("name") String name,Page<Adopt> page);
    
    @Select("SELECT a.*, u.username as user_name, an.name as animal_name, an.img as animal_img " +
           "FROM adopt a " +
           "LEFT JOIN user u ON a.user_id = u.id " +
           "LEFT JOIN animal an ON a.animal_id = an.id " +
           "WHERE a.user_id = #{userId} " +
           "ORDER BY a.id DESC")
    List<Adopt> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT DATE(time) as date, COUNT(*) as count " +
            "FROM adopt " +
            "WHERE time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(time) " +
            "ORDER BY date")
    List<Map<String, Object>> selectAdoptTrend(@Param("startDate") String startDate, 
                                             @Param("endDate") String endDate);
}
