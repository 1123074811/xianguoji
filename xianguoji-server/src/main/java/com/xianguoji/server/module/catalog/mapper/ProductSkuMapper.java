package com.xianguoji.server.module.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    @Select("SELECT COALESCE(MIN(price), 0) FROM product_sku WHERE product_id = #{productId} AND status = 1")
    BigDecimal selectMinPrice(@Param("productId") Long productId);

    @Select("SELECT COALESCE(MAX(price), 0) FROM product_sku WHERE product_id = #{productId} AND status = 1")
    BigDecimal selectMaxPrice(@Param("productId") Long productId);

    @Select("SELECT COALESCE(SUM(stock), 0) FROM product_sku WHERE product_id = #{productId}")
    Integer selectTotalStock(@Param("productId") Long productId);
}
