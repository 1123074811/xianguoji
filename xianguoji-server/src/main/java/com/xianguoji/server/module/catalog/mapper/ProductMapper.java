package com.xianguoji.server.module.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.module.catalog.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
