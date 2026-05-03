package com.xianguoji.server.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.module.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
