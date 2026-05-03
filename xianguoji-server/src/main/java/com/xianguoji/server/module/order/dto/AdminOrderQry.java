package com.xianguoji.server.module.order.dto;

import lombok.Data;

@Data
public class AdminOrderQry {

    private Integer status;
    private String keyword;
    private String startDate;
    private String endDate;
    private Integer page = 1;
    private Integer size = 20;
}
