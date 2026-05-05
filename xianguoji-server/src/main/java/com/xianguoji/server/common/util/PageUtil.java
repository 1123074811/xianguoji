package com.xianguoji.server.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.result.PageQry;
import com.xianguoji.server.common.result.PageVO;

import java.util.List;
import java.util.function.Function;

/**
 * 分页工具：简化 Page → PageVO 转换
 */
public class PageUtil {

    /**
     * 从 PageQry 构建 MyBatis-Plus Page 对象
     */
    public static <Q extends PageQry> Page<?> buildPage(Q qry) {
        return new Page<>(qry.getPage(), qry.getSize());
    }

    /**
     * 将 MyBatis-Plus Page 转换为 PageVO，并映射实体到 VO
     *
     * @param page    MP 分页结果
     * @param mapper  实体 → VO 转换函数
     * @param qry     原始查询参数（用于回填 page/size）
     */
    public static <T, V, Q extends PageQry> PageVO<V> toVO(Page<T> page, Function<T, V> mapper, Q qry) {
        List<V> voList = page.getRecords().stream().map(mapper).toList();
        return new PageVO<>(page.getTotal(), voList, qry.getPage(), qry.getSize());
    }

    /**
     * 重载：不依赖 PageQry，直接传 page/size
     */
    public static <T, V> PageVO<V> toVO(Page<T> page, Function<T, V> mapper, int pageIdx, int size) {
        List<V> voList = page.getRecords().stream().map(mapper).toList();
        return new PageVO<>(page.getTotal(), voList, pageIdx, size);
    }
}
