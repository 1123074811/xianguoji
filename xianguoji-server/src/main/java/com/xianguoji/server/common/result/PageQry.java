package com.xianguoji.server.common.result;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQry {

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer size = 20;
}
