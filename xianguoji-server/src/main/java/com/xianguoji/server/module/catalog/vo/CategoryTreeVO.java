package com.xianguoji.server.module.catalog.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryTreeVO {

    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private List<CategoryTreeVO> children;
}
