package com.xianguoji.server.module.order.dto;

import lombok.Data;

@Data
public class OrderQry {

    private String tab; // all/pending/processing/delivering/done/aftersale
    private Integer page = 1;
    private Integer size = 20;
}
