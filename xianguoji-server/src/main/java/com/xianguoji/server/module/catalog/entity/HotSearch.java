package com.xianguoji.server.module.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hot_search")
public class HotSearch {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer sort;
    private Integer status;
}
