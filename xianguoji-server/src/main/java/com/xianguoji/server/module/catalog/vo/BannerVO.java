package com.xianguoji.server.module.catalog.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BannerVO {

    private Long id;
    private String title;
    private String image;
    private Integer linkType;
    private String linkValue;
}
