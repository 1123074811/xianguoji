package com.xianguoji.server.module.catalog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerVO {

    private Long id;
    private String title;
    private String image;
    private Integer linkType;
    private String linkValue;
}
