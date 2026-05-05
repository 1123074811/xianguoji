package com.xianguoji.server.module.catalog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeVO {

    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private List<CategoryTreeVO> children;
}
