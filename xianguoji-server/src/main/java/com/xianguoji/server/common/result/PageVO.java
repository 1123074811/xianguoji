package com.xianguoji.server.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageVO<T> {

    private long total;
    private List<T> list;
    private int page;
    private int size;
}
