package com.xianguoji.server.module.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.module.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
