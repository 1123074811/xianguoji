package com.xianguoji.server.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 单 SQL：将 tag='regular' 且 30 天内无下单的用户标记为 'silent'
     */
    @Update("UPDATE user SET tag = 'silent', updated_at = NOW() " +
            "WHERE tag = 'regular' " +
            "AND id NOT IN (" +
            "  SELECT DISTINCT user_id FROM `order` WHERE created_at > #{threshold}" +
            ")")
    int updateToSilent(@Param("threshold") LocalDateTime threshold);
}
