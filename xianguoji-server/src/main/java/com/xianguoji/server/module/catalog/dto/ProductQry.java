package com.xianguoji.server.module.catalog.dto;

import lombok.Data;

@Data
public class ProductQry {

    private Long categoryId;
    private String keyword;
    private String sort; // comprehensive / sales / priceAsc / priceDesc
    private Integer page = 1;
    private Integer size = 20;
}
